package com.github.dgraciac.euclideantsp

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

internal class BruteForceTest {
    private val algorithm: BruteForce = BruteForce()

    @ParameterizedTest
    @ArgumentsSource(TSPInstancesProvider::class)
    fun solve_instances(instance: Euclidean2DTSPInstance) {
        algorithm.compute(instance).let {
            println("Brute force solution: $it")
            println("==============================================")
        }
    }
}