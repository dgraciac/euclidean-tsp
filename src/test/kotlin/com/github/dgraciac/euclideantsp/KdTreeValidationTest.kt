package com.github.dgraciac.euclideantsp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Validacion de que buildNeighborListsKdTree devuelve los mismos K vecinos
 * que buildNeighborLists (brute force sort).
 */
internal class KdTreeValidationTest {
    @Test
    fun kdtree_matches_brute_force_on_a280() {
        val points = A_280.points
        val k = 7

        val bruteForce = buildNeighborLists(points, k)
        val kdTree = buildNeighborListsKdTree(points, k)

        var mismatches = 0
        for (point in points) {
            val bfNeighbors = bruteForce[point]!!.toSet()
            val kdNeighbors = kdTree[point]!!.toSet()
            if (bfNeighbors != kdNeighbors) {
                mismatches++
                if (mismatches <= 5) {
                    println("MISMATCH for $point:")
                    println("  BF: ${bruteForce[point]!!.map { "$it d=${point.distance(it)}" }}")
                    println("  KD: ${kdTree[point]!!.map { "$it d=${point.distance(it)}" }}")
                }
            }
        }

        println("Total mismatches: $mismatches / ${points.size}")
        assertThat(mismatches).isEqualTo(0)
    }
}
