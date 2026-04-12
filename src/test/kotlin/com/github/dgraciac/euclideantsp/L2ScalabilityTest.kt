package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E042 — Verificar que la ventaja de velocidad de L2 sobre J5 se mantiene en instancias grandes.
 *
 * Ejecuta J5 y L2 en instancias de n=657 a n=2103 y compara tiempos y calidad.
 * Incluye u1060 (n=1060) y fl1577 (n=1577) como instancias nuevas.
 */
internal class L2ScalabilityTest {
    private val solverJ5 = SolverJ5()
    private val solverL2 = SolverL2()

    @Test
    fun scalability_comparison() {
        val instances =
            listOf(
                D_657,
                RAT_783,
                PR_1002,
                U_1060,
                D_1291,
                FL_1577,
                D_2103,
            )

        println("=== L2 vs J5 escalabilidad (n=657 a n=2103) ===")
        println("%-12s %5s | %8s %8s | %8s %8s | %6s".format("Instancia", "n", "J5 ratio", "J5 tiempo", "L2 ratio", "L2 tiempo", "L2/J5"))

        for (instance in instances) {
            var j5Tour: Tour? = null
            val j5Time = measureTimeMillis { j5Tour = solverJ5.compute(instance) }
            val j5Ratio = j5Tour!!.length / instance.optimalLength

            var l2Tour: Tour? = null
            val l2Time = measureTimeMillis { l2Tour = solverL2.compute(instance) }
            val l2Ratio = l2Tour!!.length / instance.optimalLength

            val speedup = j5Time.toDouble() / l2Time

            println(
                "%-12s %5d | %8.4fx %7.1fs | %8.4fx %7.1fs | %5.2fx".format(
                    instance.name,
                    instance.points.size,
                    j5Ratio,
                    j5Time / 1000.0,
                    l2Ratio,
                    l2Time / 1000.0,
                    speedup,
                ),
            )
        }
    }
}
