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
                "SolverB" to SolverB(),
                "SolverB1" to SolverB1(),
                "SolverC1" to SolverC1(),
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
