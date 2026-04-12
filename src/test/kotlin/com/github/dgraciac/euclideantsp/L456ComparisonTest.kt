package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E044/E045/E046 — Comparar L4 (matriz distancias), L5 (K=5+5), L6 (or-opt-nl) vs L2.
 * Cada uno aísla una optimizacion de velocidad. L2 es la referencia.
 */
internal class L456ComparisonTest {
    @Test
    fun compare_all() {
        val solvers: List<Pair<String, Euclidean2DTSPSolver>> =
            listOf(
                "L2 (ref)" to SolverL2(),
                "L4 (dist matrix)" to SolverL4(),
                "L5 (K=5+5)" to SolverL5(),
                "L6 (or-opt-nl)" to SolverL6(),
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

        println("=== L4/L5/L6 vs L2 ===")
        for (instance in instances) {
            val results = mutableListOf<Triple<String, Double, Long>>()
            for ((name, solver) in solvers) {
                var tour: Tour? = null
                val time = measureTimeMillis { tour = solver.compute(instance) }
                val ratio = tour!!.length / instance.optimalLength
                results.add(Triple(name, ratio, time))
            }

            val l2Time = results[0].third
            print("${instance.name} (n=${instance.points.size}): ")
            for ((name, ratio, time) in results) {
                val speedup = if (time > 0) l2Time.toDouble() / time else 0.0
                print("$name=${"%.4f".format(ratio)}x ${time / 1000.0}s (${"%.1f".format(speedup)}x)  ")
            }
            println()
        }
    }
}
