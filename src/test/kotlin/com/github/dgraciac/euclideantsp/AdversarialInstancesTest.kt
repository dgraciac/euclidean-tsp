package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * E033 — Busqueda de contraejemplos adversariales para la garantia de aproximacion.
 *
 * Genera instancias diseñadas para ser dificiles para heuristicas basadas en 2-opt/NN:
 * 1. Grid regular: NN tiende a crear caminos serpentino suboptimos
 * 2. Clusters separados: facil equivocarse en la conexion entre clusters
 * 3. Espiral: NN sigue la espiral pero el tour optimo cruza
 * 4. Anillo con puntos interiores: 2-opt se queda en optimos locales malos
 *
 * Para cada instancia se compara SolverJ5 y Christofides.
 * El objetivo es encontrar instancias donde SolverJ5 tenga ratio > 1.5 (peor que Christofides).
 */
internal class AdversarialInstancesTest {
    @Test
    fun grid_instances() {
        // Grids de distintos tamaños
        for (size in listOf(5, 7, 10, 15, 20)) {
            val points = mutableSetOf<Point>()
            for (x in 0 until size) {
                for (y in 0 until size) {
                    points.add(Point(x.toDouble(), y.toDouble()))
                }
            }
            // Tour optimo de un grid n×n: recorrido serpentino, longitud = n^2 - 1 + (n-1)
            // Aproximacion: longitud optima ≈ n^2 (para grids grandes)
            val optimalEstimate = estimateGridOptimal(size)
            val instance =
                Euclidean2DTSPInstance(
                    name = "grid_${size}x$size",
                    points = points,
                    optimalLength = optimalEstimate,
                )
            runComparison(instance)
        }
    }

    @Test
    fun cluster_instances() {
        // Clusters bien separados
        for (numClusters in listOf(3, 5, 8)) {
            val pointsPerCluster = 20
            val clusterSpacing = 100.0
            val clusterRadius = 5.0
            val points = mutableSetOf<Point>()

            for (c in 0 until numClusters) {
                val cx = c * clusterSpacing
                val cy = 0.0
                for (i in 0 until pointsPerCluster) {
                    val angle = 2.0 * PI * i / pointsPerCluster
                    points.add(Point(cx + clusterRadius * cos(angle), cy + clusterRadius * sin(angle)))
                }
            }

            // Optimo estimado: perimetro de cada cluster + distancia entre clusters
            val clusterPerimeter = 2.0 * PI * clusterRadius
            val interClusterDist = (numClusters - 1) * clusterSpacing * 2
            val optEstimate = numClusters * clusterPerimeter + interClusterDist * 0.5

            val instance =
                Euclidean2DTSPInstance(
                    name = "clusters_${numClusters}x$pointsPerCluster",
                    points = points,
                    optimalLength = optEstimate,
                )
            runComparison(instance)
        }
    }

    @Test
    fun spiral_instances() {
        for (n in listOf(50, 100, 200)) {
            val points = mutableSetOf<Point>()
            for (i in 0 until n) {
                val t = i.toDouble() / n * 4 * PI
                val r = t * 10
                points.add(Point(r * cos(t), r * sin(t)))
            }

            // Optimo: seguir la espiral de fuera a dentro y volver
            var optEstimate = 0.0
            val sortedByAngle = points.sortedBy { kotlin.math.atan2(it.y, it.x) + sqrt(it.x * it.x + it.y * it.y) / 1000 }
            for (i in 0 until sortedByAngle.size - 1) {
                optEstimate += sortedByAngle[i].distance(sortedByAngle[i + 1])
            }
            optEstimate += sortedByAngle.last().distance(sortedByAngle.first())

            val instance =
                Euclidean2DTSPInstance(
                    name = "spiral_$n",
                    points = points,
                    optimalLength = optEstimate,
                )
            runComparison(instance)
        }
    }

    @Test
    fun ring_with_interior() {
        // Anillo exterior con puntos interiores aleatorios (seed fijo)
        for (n in listOf(50, 100, 200)) {
            val points = mutableSetOf<Point>()
            val ringPoints = n / 2
            val interiorPoints = n - ringPoints
            val radius = 100.0

            // Anillo
            for (i in 0 until ringPoints) {
                val angle = 2.0 * PI * i / ringPoints
                points.add(Point(radius * cos(angle), radius * sin(angle)))
            }

            // Interior (determinista con patron regular)
            val innerRadius = radius * 0.6
            for (i in 0 until interiorPoints) {
                val angle = 2.0 * PI * i / interiorPoints + 0.1
                val r = innerRadius * (i + 1.0) / interiorPoints
                points.add(Point(r * cos(angle), r * sin(angle)))
            }

            // Optimo estimado: perimetro del anillo (cota inferior)
            val optEstimate = 2.0 * PI * radius

            val instance =
                Euclidean2DTSPInstance(
                    name = "ring_interior_$n",
                    points = points,
                    optimalLength = optEstimate,
                )
            runComparison(instance)
        }
    }

    private fun runComparison(instance: Euclidean2DTSPInstance) {
        val christofides = Christofides()
        val solverJ5 = SolverJ5()

        var christofidesTour: Tour? = null
        val christofidesTime =
            measureTimeMillis {
                christofidesTour = christofides.compute(instance)
            }

        var j5Tour: Tour? = null
        val j5Time =
            measureTimeMillis {
                j5Tour = solverJ5.compute(instance)
            }

        val cRatio = christofidesTour!!.length / instance.optimalLength
        val jRatio = j5Tour!!.length / instance.optimalLength
        val worse = if (jRatio > cRatio) " *** J5 PEOR QUE CHRISTOFIDES ***" else ""

        println(
            "${instance.name} (n=${instance.points.size}): " +
                "Christofides=${"%.4f".format(cRatio)}x (${christofidesTime}ms) | " +
                "SolverJ5=${"%.4f".format(jRatio)}x (${j5Time}ms)$worse",
        )
    }

    private fun estimateGridOptimal(size: Int): Double {
        // Tour optimo serpentino de un grid n×n
        // n columnas, cada una de n puntos. Serpenteo: (n-1)*n aristas verticales + (n-1) horizontales
        // Total: n*(n-1) + (n-1) = (n-1)*(n+1) = n^2 - 1
        // Mas la arista de cierre: distancia del ultimo al primer punto
        val n = size
        return (n.toLong() * n - 1).toDouble() + sqrt(((n - 1) * (n - 1) + 0).toDouble())
    }
}
