package com.github.dgraciac.euclideantsp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Validacion de que buildAlphaNearnessHld devuelve los mismos K candidatos
 * que buildAlphaNearnessListLean.
 */
internal class AlphaHldValidationTest {
    @Test
    fun hld_matches_lean_on_pcb442() {
        val points = PCB_442.points
        val k = 7

        val lean = buildAlphaNearnessListLean(points, k)
        val hld = buildAlphaNearnessHld(points, k)

        var mismatches = 0
        for (point in points) {
            val leanNeighbors = lean[point]!!
            val hldNeighbors = hld[point]!!
            if (leanNeighbors.toSet() != hldNeighbors.toSet()) {
                mismatches++
                if (mismatches <= 3) {
                    println("MISMATCH for $point:")
                    println("  LEAN: $leanNeighbors")
                    println("  HLD:  $hldNeighbors")
                }
            }
        }

        println("Total mismatches: $mismatches / ${points.size}")
        assertThat(mismatches).isEqualTo(0)
    }
}
