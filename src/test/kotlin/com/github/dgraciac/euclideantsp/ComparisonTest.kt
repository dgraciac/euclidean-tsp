package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.system.measureTimeMillis

internal class ComparisonTest {
    companion object {
        val solvers: List<Pair<String, Euclidean2DTSPSolver>> =
            listOf(
                "Christofides" to Christofides(),
                // "SolverB" to SolverB(),   // O(n^4) — descomentar solo para comparacion completa
                // "SolverB1" to SolverB1(), // O(n^4) — descomentar solo para comparacion completa
                // "SolverB2" to SolverB2(), // Superado por SolverC3
                // "SolverC1" to SolverC1(), // Superado por SolverC3
                // "SolverC2" to SolverC2(), // Superado por SolverC3
                "SolverC3" to SolverC3(),
                // "SolverC4" to SolverC4(), // Superado por SolverC3
                // "SolverB3" to SolverB3(), // Superado por SolverE2
                // "SolverE1" to SolverE1(), // Superado por SolverE2
                // "SolverF1" to SolverF1(), // Superado por SolverE2
                "SolverE2" to SolverE2(),
                "SolverE3" to SolverE3(),
                // "SolverE4" to SolverE4(), // Identico a SolverE2 (E014)
                // "SolverE5" to SolverE5(), // Superado por SolverE7
                // "SolverE6" to SolverE6(), // Superado por SolverE7
                "SolverE7" to SolverE7(),
                "SolverG1" to SolverG1(),
                "SolverG2" to SolverG2(),
                "SolverH1" to SolverH1(),
            )
    }

    @ParameterizedTest
    @ArgumentsSource(TSPInstanceProvider::class)
    fun solve_instances(instance: Euclidean2DTSPInstance) {
        println("Instance name: ${instance.name}")

        solvers.forEach { (name, solver) ->
            var tour: Tour? = null
            val timeMs =
                measureTimeMillis {
                    tour = solver.compute(instance)
                }
            val tourLength = tour?.length
            val approximation = tourLength?.div(instance.optimalLength)
            println(
                "Algorithm: $name; Time: ${timeMs / 1000.0}s; Length: $tourLength; Approximation: $approximation",
            )
        }

        println("=====================================================================================================")
    }
}
