package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * E033 — Busqueda empirica de la peor aproximacion de SolverJ5.
 *
 * Genera muchas instancias pequeñas (n=8 a n=12) con patrones adversariales,
 * calcula el tour optimo con BruteForce, y mide el ratio de SolverJ5.
 * El peor ratio encontrado es una cota inferior de la garantia de aproximacion.
 *
 * Si encontramos un ratio > 1.5, SolverJ5 es peor que Christofides en el peor caso.
 * Si no lo encontramos, es evidencia (no prueba) de que podria ser mejor.
 */
internal class ApproximationGuaranteeTest {
    private val bruteForce = BruteForce()
    private val christofides = Christofides()
    private val solverJ5 = SolverJ5()

    @Test
    fun worst_case_search_grids() {
        // Grids pequeños donde BruteForce es viable
        var worstJ5Ratio = 0.0
        var worstCRatio = 0.0
        var worstInstance = ""

        for (cols in 2..4) {
            for (rows in 2..4) {
                if (cols * rows > 12 || cols * rows < 4) continue
                val points = mutableSetOf<Point>()
                for (x in 0 until cols) {
                    for (y in 0 until rows) {
                        points.add(Point(x.toDouble(), y.toDouble()))
                    }
                }
                val name = "grid_${cols}x$rows"
                val (jRatio, cRatio) = testInstance(name, points)
                if (jRatio > worstJ5Ratio) {
                    worstJ5Ratio = jRatio
                    worstCRatio = cRatio
                    worstInstance = name
                }
            }
        }
        println("Peor grid para J5: $worstInstance ratio=${"%.6f".format(worstJ5Ratio)} (Christofides=${"%.6f".format(worstCRatio)})")
    }

    @Test
    fun worst_case_search_clusters() {
        var worstJ5Ratio = 0.0
        var worstCRatio = 0.0
        var worstInstance = ""

        // Clusters de distintas separaciones
        for (sep in listOf(5, 10, 20, 50, 100)) {
            for (pointsPerCluster in 3..4) {
                val points = mutableSetOf<Point>()
                for (c in 0..1) {
                    for (i in 0 until pointsPerCluster) {
                        val angle = 2.0 * PI * i / pointsPerCluster
                        points.add(Point(c * sep + cos(angle), sin(angle)))
                    }
                }
                val name = "2clusters_sep${sep}_${pointsPerCluster}pp"
                val (jRatio, cRatio) = testInstance(name, points)
                if (jRatio > worstJ5Ratio) {
                    worstJ5Ratio = jRatio
                    worstCRatio = cRatio
                    worstInstance = name
                }
            }
        }
        println("Peor cluster para J5: $worstInstance ratio=${"%.6f".format(worstJ5Ratio)} (Christofides=${"%.6f".format(worstCRatio)})")
    }

    @Test
    fun worst_case_search_random_patterns() {
        var worstJ5Ratio = 0.0
        var worstCRatio = 0.0
        var worstInstance = ""

        // Patrones deterministas variados
        for (n in 6..10) {
            // Patron 1: puntos en linea con perturbaciones
            for (perturbation in listOf(0.1, 0.5, 1.0, 2.0)) {
                val points = mutableSetOf<Point>()
                for (i in 0 until n) {
                    val y = if (i % 2 == 0) perturbation else -perturbation
                    points.add(Point(i.toDouble(), y))
                }
                val name = "zigzag_${n}_p$perturbation"
                val (jRatio, cRatio) = testInstance(name, points)
                if (jRatio > worstJ5Ratio) {
                    worstJ5Ratio = jRatio
                    worstCRatio = cRatio
                    worstInstance = name
                }
            }

            // Patron 2: estrella (centro + puntos radiales)
            if (n >= 5) {
                val points = mutableSetOf<Point>()
                points.add(Point(0.0, 0.0))
                for (i in 1 until n) {
                    val angle = 2.0 * PI * i / (n - 1)
                    val r = if (i % 2 == 0) 10.0 else 5.0
                    points.add(Point(r * cos(angle), r * sin(angle)))
                }
                val name = "star_$n"
                val (jRatio, cRatio) = testInstance(name, points)
                if (jRatio > worstJ5Ratio) {
                    worstJ5Ratio = jRatio
                    worstCRatio = cRatio
                    worstInstance = name
                }
            }

            // Patron 3: dos filas paralelas
            if (n >= 6 && n % 2 == 0) {
                val points = mutableSetOf<Point>()
                for (i in 0 until n / 2) {
                    points.add(Point(i.toDouble() * 10, 0.0))
                    points.add(Point(i.toDouble() * 10 + 5, 1.0))
                }
                val name = "tworows_$n"
                val (jRatio, cRatio) = testInstance(name, points)
                if (jRatio > worstJ5Ratio) {
                    worstJ5Ratio = jRatio
                    worstCRatio = cRatio
                    worstInstance = name
                }
            }
        }
        println(
            "Peor patron para J5: $worstInstance ratio=${"%.6f".format(worstJ5Ratio)} " +
                "(Christofides=${"%.6f".format(worstCRatio)})",
        )
    }

    @Test
    fun summary_all_tsplib() {
        // Resumen en instancias TSPLIB reales
        println("=== Ratios en instancias TSPLIB (optimo conocido) ===")
        val instances =
            listOf(
                EIL_51,
                BERLIN_52,
                ST_70,
                EIL_76,
                RAT_99,
                KRO_200,
                A_280,
                PCB_442,
            )
        var worstJ5 = 0.0
        var worstC = 0.0
        for (instance in instances) {
            val cTour = christofides.compute(instance)
            val jTour = solverJ5.compute(instance)
            val cRatio = cTour.length / instance.optimalLength
            val jRatio = jTour.length / instance.optimalLength
            println(
                "  ${instance.name} (n=${instance.points.size}): " +
                    "J5=${"%.4f".format(jRatio)}x  Christofides=${"%.4f".format(cRatio)}x",
            )
            if (jRatio > worstJ5) worstJ5 = jRatio
            if (cRatio > worstC) worstC = cRatio
        }
        println("Peor ratio J5: ${"%.4f".format(worstJ5)}x | Peor ratio Christofides: ${"%.4f".format(worstC)}x")
    }

    private fun testInstance(
        name: String,
        points: Set<Point>,
    ): Pair<Double, Double> {
        val instance =
            Euclidean2DTSPInstance(
                name = name,
                points = points,
                optimalLength = 1.0, // Placeholder, usaremos BruteForce
            )

        val optimalTour = bruteForce.compute(instance)
        val optimalLength = optimalTour.length

        val realInstance =
            Euclidean2DTSPInstance(
                name = name,
                points = points,
                optimalLength = optimalLength,
            )

        val cTour = christofides.compute(realInstance)
        val jTour = solverJ5.compute(realInstance)

        val cRatio = cTour.length / optimalLength
        val jRatio = jTour.length / optimalLength

        val flag = if (jRatio > cRatio) " *** J5 > C ***" else ""
        println(
            "  $name (n=${points.size}): optimal=${"%.4f".format(optimalLength)} " +
                "J5=${"%.6f".format(jRatio)}x  C=${"%.6f".format(cRatio)}x$flag",
        )

        return Pair(jRatio, cRatio)
    }
}
