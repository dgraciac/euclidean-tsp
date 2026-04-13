package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E061 — SolverM9 vs M5: KD-tree para neighborLists.
 * M9 = M5 + KD-tree. La calidad debe ser IDENTICA (mismos K vecinos).
 * Solo cambia la velocidad de la fase de construccion de neighbor lists.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M9VsM5Test {
    private val solverM5 = SolverM5()
    private val solverM9 = SolverM9()

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

        var m5Tour: Tour? = null
        val m5Time = measureTimeMillis { m5Tour = solverM5.compute(instance) }
        val m5Ratio = m5Tour!!.length / instance.optimalLength

        var m9Tour: Tour? = null
        val m9Time = measureTimeMillis { m9Tour = solverM9.compute(instance) }
        val m9Ratio = m9Tour!!.length / instance.optimalLength

        val speedup = m5Time.toDouble() / m9Time
        val diff = m9Ratio - m5Ratio

        println(
            "M9vsM5 | %-12s | n=%5d | M5=%7.4fx %7.1fs | M9=%7.4fx %7.1fs | speedup=%5.2fx | diff=%+.4f".format(
                instance.name,
                n,
                m5Ratio,
                m5Time / 1000.0,
                m9Ratio,
                m9Time / 1000.0,
                speedup,
                diff,
            ),
        )
    }
}
