package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.system.measureTimeMillis

/**
 * E047 Fase 3 — Verificar que L7 (DistanceMatrix) da resultados identicos a L2
 * y medir el speedup.
 */
internal class L7VsL2Test {
    private val solverL2 = SolverL2()
    private val solverL7 = SolverL7()

    @Test
    fun verify_identical_results_and_speedup() {
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
                D_657,
                RAT_783,
                PR_1002,
            )

        println("=== L7 (DistanceMatrix) vs L2 ===")
        println(
            "%-12s %5s | %8s %8s | %8s %8s | %6s %s".format(
                "Instancia",
                "n",
                "L2 ratio",
                "L2 t(s)",
                "L7 ratio",
                "L7 t(s)",
                "speedup",
                "identico?",
            ),
        )

        for (instance in instances) {
            var l2Tour: Tour? = null
            val l2Time = measureTimeMillis { l2Tour = solverL2.compute(instance) }
            val l2Ratio = l2Tour!!.length / instance.optimalLength

            var l7Tour: Tour? = null
            val l7Time = measureTimeMillis { l7Tour = solverL7.compute(instance) }
            val l7Ratio = l7Tour!!.length / instance.optimalLength

            val speedup = l2Time.toDouble() / l7Time
            val diff = abs(l7Ratio - l2Ratio)
            val identical = if (diff < 0.0001) "SI" else "NO (diff=${"%.6f".format(diff)})"

            println(
                "%-12s %5d | %8.4fx %7.1fs | %8.4fx %7.1fs | %5.2fx %s".format(
                    instance.name,
                    instance.points.size,
                    l2Ratio,
                    l2Time / 1000.0,
                    l7Ratio,
                    l7Time / 1000.0,
                    speedup,
                    identical,
                ),
            )
        }
    }
}
