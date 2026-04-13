package com.github.dgraciac.euclideantsp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * Validacion de que buildAlphaNearnessDelaunay produce los mismos candidatos
 * que buildAlphaNearnessListLean, y medicion de speedup.
 */
internal class AlphaDelaunayValidationTest {
    @Test
    fun delaunay_matches_lean_on_pcb442() {
        val points = PCB_442.points
        val k = 7

        var lean: Map<com.github.dgraciac.euclideantsp.shared.Point, List<com.github.dgraciac.euclideantsp.shared.Point>>? = null
        val leanTime = measureTimeMillis { lean = buildAlphaNearnessListLean(points, k) }

        var delaunay: Map<com.github.dgraciac.euclideantsp.shared.Point, List<com.github.dgraciac.euclideantsp.shared.Point>>? = null
        val delaunayTime = measureTimeMillis { delaunay = buildAlphaNearnessDelaunay(points, k) }

        var mismatches = 0
        for (point in points) {
            val leanNeighbors = lean!![point]!!.toSet()
            val delNeighbors = delaunay!![point]!!.toSet()
            if (leanNeighbors != delNeighbors) {
                mismatches++
                if (mismatches <= 3) {
                    println("MISMATCH for $point:")
                    println("  LEAN:     ${lean!![point]}")
                    println("  DELAUNAY: ${delaunay!![point]}")
                }
            }
        }

        println("Total mismatches: $mismatches / ${points.size}")
        println(
            "Lean time: ${leanTime}ms | Delaunay time: ${delaunayTime}ms | Speedup: ${"%.2f".format(leanTime.toDouble() / delaunayTime)}x",
        )
        assertThat(mismatches).isEqualTo(0)
    }
}
