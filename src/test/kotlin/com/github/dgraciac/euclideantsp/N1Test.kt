package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E055 — SolverN1: Insercion geometrica desde convex hull guiada por Delaunay.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class N1Test {
    private val solverN1 = SolverN1()

    @Test
    @Order(1)
    fun n1_eil51() = runInstance(EIL_51)

    @Test
    @Order(2)
    fun n1_berlin52() = runInstance(BERLIN_52)

    @Test
    @Order(3)
    fun n1_st70() = runInstance(ST_70)

    @Test
    @Order(4)
    fun n1_eil76() = runInstance(EIL_76)

    @Test
    @Order(5)
    fun n1_rat99() = runInstance(RAT_99)

    @Test
    @Order(6)
    fun n1_kro200() = runInstance(KRO_200)

    @Test
    @Order(7)
    fun n1_a280() = runInstance(A_280)

    @Test
    @Order(8)
    fun n1_pcb442() = runInstance(PCB_442)

    @Test
    @Order(9)
    fun n1_d657() = runInstance(D_657)

    @Test
    @Order(10)
    fun n1_pr1002() = runInstance(PR_1002)

    @Test
    @Order(11)
    fun n1_d2103() = runInstance(D_2103)

    @Test
    @Order(20)
    fun n1_pcb3038() = runInstance(PCB_3038)

    @Test
    @Order(21)
    fun n1_rl5915() = runInstance(RL_5915)

    private fun runInstance(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solverN1.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        val timeSec = timeMs / 1000.0

        println(
            "N1 | %-12s | n=%6d | ratio=%8.4fx | time=%9.1fs".format(
                instance.name,
                n,
                ratio,
                timeSec,
            ),
        )
    }
}
