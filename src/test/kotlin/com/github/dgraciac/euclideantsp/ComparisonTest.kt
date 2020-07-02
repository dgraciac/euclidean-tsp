package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import kotlin.system.measureTimeMillis
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

internal class ComparisonTest {
    private val bruteForce: BruteForce = BruteForce()
    private val christofides: Christofides = Christofides()

    @ParameterizedTest
    @ArgumentsSource(TSPInstancesProvider::class)
    fun solve_instances(instance: Euclidean2DTSPInstance) {
        var minimalLength: Double
        var bruteForceTour: Tour? = null
        measureTimeMillis { bruteForceTour = bruteForce.compute(instance) }.let {
            minimalLength = bruteForceTour!!.length
            println("Algorithm: Brute force; Instance name: ${instance.name}; Time: ${it / 1000.0}s; Length: $minimalLength")
            println("==============================================")
        }

        var christofidesTour: Tour? = null
        measureTimeMillis { christofidesTour = christofides.compute(instance) }.let {
            println("Algorithm: Christofides; Instance name: ${instance.name}; Time: ${it / 1000.0}s; Length: ${christofidesTour?.length}; Approximation: ${christofidesTour?.length?.div(minimalLength)}")
            println("==============================================")
        }
    }
}
