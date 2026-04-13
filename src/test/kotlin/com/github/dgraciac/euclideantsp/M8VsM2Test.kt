package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E060 — SolverM8 vs M2: LK-deep(3, alphaNl K=7) con post-DB.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M8VsM2Test {
    private val solverM2 = SolverM2()
    private val solverM8 = SolverM8()

    @Test
    @Order(1)
    fun cmp_eil51() = compare(EIL_51)

    @Test
    @Order(2)
    fun cmp_berlin52() = compare(BERLIN_52)

    @Test
    @Order(3)
    fun cmp_st70() = compare(ST_70)

    @Test
    @Order(4)
    fun cmp_eil76() = compare(EIL_76)

    @Test
    @Order(5)
    fun cmp_rat99() = compare(RAT_99)

    @Test
    @Order(6)
    fun cmp_kro200() = compare(KRO_200)

    @Test
    @Order(7)
    fun cmp_a280() = compare(A_280)

    @Test
    @Order(8)
    fun cmp_pcb442() = compare(PCB_442)

    @Test
    @Order(9)
    fun cmp_d657() = compare(D_657)

    @Test
    @Order(10)
    fun cmp_pr1002() = compare(PR_1002)

    @Test
    @Order(11)
    fun cmp_d2103() = compare(D_2103)

    @Test
    @Order(12)
    fun cmp_pcb3038() = compare(PCB_3038)

    private fun compare(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size

        var m2Tour: Tour? = null
        val m2Time = measureTimeMillis { m2Tour = solverM2.compute(instance) }
        val m2Ratio = m2Tour!!.length / instance.optimalLength

        var m8Tour: Tour? = null
        val m8Time = measureTimeMillis { m8Tour = solverM8.compute(instance) }
        val m8Ratio = m8Tour!!.length / instance.optimalLength

        val speedup = m2Time.toDouble() / m8Time
        val diff = m8Ratio - m2Ratio

        println(
            "M8vsM2 | %-12s | n=%5d | M2=%7.4fx %7.1fs | M8=%7.4fx %7.1fs | speedup=%5.2fx | diff=%+.4f".format(
                instance.name,
                n,
                m2Ratio,
                m2Time / 1000.0,
                m8Ratio,
                m8Time / 1000.0,
                speedup,
                diff,
            ),
        )
    }
}
