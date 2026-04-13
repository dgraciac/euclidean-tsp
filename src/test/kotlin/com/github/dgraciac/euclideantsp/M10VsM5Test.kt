package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E062 — SolverM10 vs M5: alpha-nearness con binary lifting + KD-tree.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M10VsM5Test {
    private val solverM5 = SolverM5()
    private val solverM10 = SolverM10()

    @Test
    @Order(1)
    fun cmp_pcb442() = compare(PCB_442)

    @Test
    @Order(2)
    fun cmp_d657() = compare(D_657)

    @Test
    @Order(3)
    fun cmp_pr1002() = compare(PR_1002)

    @Test
    @Order(4)
    fun cmp_d2103() = compare(D_2103)

    @Test
    @Order(5)
    fun cmp_pcb3038() = compare(PCB_3038)

    private fun compare(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size

        var m5Tour: Tour? = null
        val m5Time = measureTimeMillis { m5Tour = solverM5.compute(instance) }
        val m5Ratio = m5Tour!!.length / instance.optimalLength

        var m10Tour: Tour? = null
        val m10Time = measureTimeMillis { m10Tour = solverM10.compute(instance) }
        val m10Ratio = m10Tour!!.length / instance.optimalLength

        val speedup = m5Time.toDouble() / m10Time
        val diff = m10Ratio - m5Ratio

        println(
            "M10vsM5 | %-12s | n=%5d | M5=%7.4fx %7.1fs | M10=%7.4fx %7.1fs | speedup=%5.2fx | diff=%+.4f".format(
                instance.name,
                n,
                m5Ratio,
                m5Time / 1000.0,
                m10Ratio,
                m10Time / 1000.0,
                speedup,
                diff,
            ),
        )
    }
}
