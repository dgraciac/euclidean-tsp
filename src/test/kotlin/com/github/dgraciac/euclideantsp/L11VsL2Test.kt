package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E051 — L11 (DB α-nearness reemplaza DB estandar) vs L2.
 */
internal class L11VsL2Test {
    @Test
    fun compare() {
        val solverL2 = SolverL2()
        val solverL11 = SolverL11()

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

        println("=== L11 (DB α-only) vs L2 ===")
        println(
            "%-12s %5s | %8s %8s | %8s %8s | %6s %s".format(
                "Instancia",
                "n",
                "L2 ratio",
                "L2 t(s)",
                "L11 ratio",
                "L11 t(s)",
                "speedup",
                "calidad",
            ),
        )

        for (instance in instances) {
            var l2Tour: Tour? = null
            val l2Time = measureTimeMillis { l2Tour = solverL2.compute(instance) }
            val l2Ratio = l2Tour!!.length / instance.optimalLength

            var l11Tour: Tour? = null
            val l11Time = measureTimeMillis { l11Tour = solverL11.compute(instance) }
            val l11Ratio = l11Tour!!.length / instance.optimalLength

            val speedup = l2Time.toDouble() / l11Time
            val diff = l11Ratio - l2Ratio
            val quality =
                when {
                    diff < -0.0005 -> "MEJOR (%.3f%%)".format(-diff * 100)
                    diff > 0.0005 -> "peor (%.3f%%)".format(diff * 100)
                    else -> "igual"
                }

            println(
                "%-12s %5d | %8.4fx %7.1fs | %8.4fx %7.1fs | %5.2fx %s".format(
                    instance.name,
                    instance.points.size,
                    l2Ratio,
                    l2Time / 1000.0,
                    l11Ratio,
                    l11Time / 1000.0,
                    speedup,
                    quality,
                ),
            )
        }
    }
}
