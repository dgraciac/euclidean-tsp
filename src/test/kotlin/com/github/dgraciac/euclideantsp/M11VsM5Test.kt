package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E063 — SolverM11 vs M5: alpha-nearness via Delaunay MST.
 * Calidad debe ser identica. Speedup esperado en n>1000 donde Prim O(n^2) domina.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M11VsM5Test {
    private val solverM5 = SolverM5()
    private val solverM11 = SolverM11()

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

        var m11Tour: Tour? = null
        val m11Time = measureTimeMillis { m11Tour = solverM11.compute(instance) }
        val m11Ratio = m11Tour!!.length / instance.optimalLength

        val speedup = m5Time.toDouble() / m11Time
        val diff = m11Ratio - m5Ratio

        println(
            "M11vsM5 | %-12s | n=%5d | M5=%7.4fx %7.1fs | M11=%7.4fx %7.1fs | speedup=%5.2fx | diff=%+.4f".format(
                instance.name,
                n,
                m5Ratio,
                m5Time / 1000.0,
                m11Ratio,
                m11Time / 1000.0,
                speedup,
                diff,
            ),
        )
    }
}
