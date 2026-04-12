package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E048/E049/E050 — Tres mejoras de calidad comparadas vs L2.
 * L8: mas DB profundos (sin LK-deep)
 * L9: DB con candidatos α-nearness
 * L10: LK final con K=20
 */
internal class L8910ComparisonTest {
    @Test
    fun compare_all() {
        val solvers: List<Pair<String, Euclidean2DTSPSolver>> =
            listOf(
                "L2 (ref)" to SolverL2(),
                "L8 (mas DB)" to SolverL8(),
                "L9 (DB alpha)" to SolverL9(),
                "L10 (LK K=20)" to SolverL10(),
            )

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

        println("=== L8/L9/L10 vs L2 (mejoras de calidad) ===")
        for (instance in instances) {
            val results = mutableListOf<Triple<String, Double, Long>>()
            for ((name, solver) in solvers) {
                var tour: Tour? = null
                val time = measureTimeMillis { tour = solver.compute(instance) }
                val ratio = tour!!.length / instance.optimalLength
                results.add(Triple(name, ratio, time))
            }

            val l2Ratio = results[0].second
            print("${instance.name} (n=${instance.points.size}): ")
            for ((name, ratio, time) in results) {
                val vs =
                    if (ratio < l2Ratio - 0.0005) {
                        "MEJOR"
                    } else if (ratio > l2Ratio + 0.0005) {
                        "peor"
                    } else {
                        "="
                    }
                print("$name=${"%.4f".format(ratio)}x ${time / 1000.0}s($vs)  ")
            }
            println()
        }
    }
}
