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
 * E034/E035 — Tests a gran escala (n=100-1000) con instancias adversariales.
 *
 * Genera instancias grandes diseñadas para estresar 2-opt y verifica:
 * 1. Si SolverJ5 mantiene buen ratio a gran escala
 * 2. Si la convergencia O(1) de 2-opt se mantiene
 * 3. Si el ratio crece con n (como predice la teoria Theta(log n/log log n))
 *
 * Sin acceso al optimo exacto para n grande, usamos Christofides como referencia
 * (tiene garantia 3/2) y comparamos los ratios relativos.
 */
internal class LargeScaleTest {
    private val christofides = Christofides()
    private val solverJ5 = SolverJ5()

    @Test
    fun large_grids() {
        for (size in listOf(10, 15, 20, 25, 30)) {
            val n = size * size
            val points = mutableSetOf<Point>()
            for (x in 0 until size) {
                for (y in 0 until size) {
                    points.add(Point(x.toDouble(), y.toDouble()))
                }
            }
            // Optimo serpentino estimado: (n-1) aristas de longitud 1 + (size-1) de longitud 1
            // mas la arista de cierre
            val optEstimate = (n - 1).toDouble() + sqrt(((size - 1) * (size - 1)).toDouble())
            testLarge("grid_${size}x$size", n, points, optEstimate)
        }
    }

    @Test
    fun large_clusters() {
        for (numClusters in listOf(5, 10, 20)) {
            val pointsPerCluster = 50
            val n = numClusters * pointsPerCluster
            val clusterSpacing = 100.0
            val clusterRadius = 5.0
            val points = mutableSetOf<Point>()

            for (c in 0 until numClusters) {
                val cx = c * clusterSpacing
                for (i in 0 until pointsPerCluster) {
                    val angle = 2.0 * PI * i / pointsPerCluster
                    val r = clusterRadius * sqrt((i + 1.0) / pointsPerCluster)
                    points.add(Point(cx + r * cos(angle), r * sin(angle)))
                }
            }

            // Cota inferior: MST ≈ suma de aristas cortas dentro de clusters + aristas entre clusters
            val optEstimate = numClusters * clusterRadius * 2 * PI + (numClusters - 1) * clusterSpacing
            testLarge("clusters_${numClusters}x$pointsPerCluster", n, points, optEstimate)
        }
    }

    @Test
    fun interlocking_rows() {
        // Instancia adversarial: filas intercaladas que confunden a NN y 2-opt
        for (rows in listOf(10, 20, 30)) {
            val cols = rows
            val n = rows * cols
            val points = mutableSetOf<Point>()

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    // Offset horizontal de filas pares vs impares
                    val xOffset = if (r % 2 == 0) 0.0 else 0.5
                    points.add(Point(c.toDouble() + xOffset, r.toDouble() * 2))
                }
            }

            val optEstimate = (n - 1).toDouble() + rows.toDouble()
            testLarge("interlocking_${rows}x$cols", n, points, optEstimate)
        }
    }

    @Test
    fun concentric_rings() {
        // Anillos concentricos: 2-opt tiende a conectar anillos incorrectamente
        for (numRings in listOf(5, 10, 15)) {
            val pointsPerRing = 50
            val n = numRings * pointsPerRing
            val points = mutableSetOf<Point>()

            for (ring in 0 until numRings) {
                val radius = (ring + 1) * 20.0
                for (i in 0 until pointsPerRing) {
                    val angle = 2.0 * PI * i / pointsPerRing + ring * 0.1
                    points.add(Point(radius * cos(angle), radius * sin(angle)))
                }
            }

            val optEstimate = (0 until numRings).sumOf { (it + 1) * 20.0 * 2 * PI } * 0.7
            testLarge("rings_${numRings}x$pointsPerRing", n, points, optEstimate)
        }
    }

    @Test
    fun tsplib_pcb442_regression() {
        // Verificar que pcb442 sigue dando buenos resultados
        testLarge("pcb442 (TSPLIB)", PCB_442.points.size, PCB_442.points, PCB_442.optimalLength)
    }

    private fun testLarge(
        name: String,
        n: Int,
        points: Set<Point>,
        optEstimate: Double,
    ) {
        val instance = Euclidean2DTSPInstance(name = name, points = points, optimalLength = optEstimate)

        var cTour: Tour? = null
        val cTime = measureTimeMillis { cTour = christofides.compute(instance) }

        var jTour: Tour? = null
        val jTime = measureTimeMillis { jTour = solverJ5.compute(instance) }

        val cLen = cTour!!.length
        val jLen = jTour!!.length
        val cRatio = cLen / optEstimate
        val jRatio = jLen / optEstimate

        // Ratio J5/Christofides — si > 1, J5 es peor que Christofides
        val relativeRatio = jLen / cLen

        val flag =
            when {
                relativeRatio > 1.0 -> " *** J5 PEOR QUE CHRISTOFIDES ***"
                relativeRatio > 0.95 -> " (J5 cerca de Christofides)"
                else -> ""
            }

        println(
            "$name (n=$n): J5=${"%.4f".format(jRatio)}x (${jTime / 1000.0}s) | " +
                "Christofides=${"%.4f".format(cRatio)}x (${cTime / 1000.0}s) | " +
                "J5/C=${"%.4f".format(relativeRatio)}$flag",
        )
    }
}
