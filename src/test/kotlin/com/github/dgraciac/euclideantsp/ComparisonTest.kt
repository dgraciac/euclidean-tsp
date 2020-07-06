package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import kotlin.system.measureTimeMillis
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

internal class ComparisonTest {
    private val bruteForce: BruteForce = BruteForce()
    private val christofides: Christofides = Christofides()
    private val solverA: SolverA = SolverA()

    @ParameterizedTest
    @ArgumentsSource(TSPInstanceProvider::class)
    fun solve_instances(instance: Euclidean2DTSPInstance) {
        println("Instance name: ${instance.name}")

        var bruteForceTour: Tour? = null
        println("Algorithm: BruteForce; Time: ${measureTimeMillis {
            bruteForceTour = bruteForce.compute(instance)
        } / 1000.0}s; Length: ${bruteForceTour?.length}; Approximation: ${bruteForceTour?.length?.div(instance.optimalLength)}")

        var christofidesTour: Tour? = null
        println(
            "Algorithm: Christofides; Time: ${measureTimeMillis {
                christofidesTour = christofides.compute(instance)
            } / 1000.0}s; Length: ${christofidesTour?.length}; Approximation: ${christofidesTour?.length?.div(
                instance.optimalLength
            )}"
        )

        var solverATour: Tour? = null
        println(
            "Algorithm: SolverA; Time: ${measureTimeMillis {
                solverATour = solverA.compute(instance)
            } / 1000.0}s; Length: ${solverATour?.length}; Approximation: ${solverATour?.length?.div(
                instance.optimalLength
            )}"
        )

        println("=====================================================================================================")
    }
}
