package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E043 — Comparar L3 (sin LK-deep) vs L2 (con LK-deep) en todas las instancias.
 */
internal class L3VsL2Test {
    private val solverL2 = SolverL2()
    private val solverL3 = SolverL3()

    @Test
    fun compare_l2_vs_l3() {
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
                U_1060,
                D_1291,
                FL_1577,
                D_2103,
            )

        println("=== L3 vs L2 (sin vs con LK-deep) ===")
        println(
            "%-12s %5s | %8s %8s | %8s %8s | %6s %s".format(
                "Instancia",
                "n",
                "L2 ratio",
                "L2 t(s)",
                "L3 ratio",
                "L3 t(s)",
                "speedup",
                "calidad",
            ),
        )

        for (instance in instances) {
            var l2Tour: Tour? = null
            val l2Time = measureTimeMillis { l2Tour = solverL2.compute(instance) }
            val l2Ratio = l2Tour!!.length / instance.optimalLength

            var l3Tour: Tour? = null
            val l3Time = measureTimeMillis { l3Tour = solverL3.compute(instance) }
            val l3Ratio = l3Tour!!.length / instance.optimalLength

            val speedup = l2Time.toDouble() / l3Time
            val qualityDiff = l3Ratio - l2Ratio
            val qualityStr =
                when {
                    qualityDiff < -0.001 -> "L3 MEJOR"
                    qualityDiff > 0.001 -> "L3 peor (%.3f%%)".format(qualityDiff * 100)
                    else -> "igual"
                }

            println(
                "%-12s %5d | %8.4fx %7.1fs | %8.4fx %7.1fs | %5.2fx %s".format(
                    instance.name,
                    instance.points.size,
                    l2Ratio,
                    l2Time / 1000.0,
                    l3Ratio,
                    l3Time / 1000.0,
                    speedup,
                    qualityStr,
                ),
            )
        }
    }
}
