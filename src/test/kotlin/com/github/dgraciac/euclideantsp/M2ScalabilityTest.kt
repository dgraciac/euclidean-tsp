package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E053 — SolverM2: escalabilidad con infraestructura optimizada.
 *
 * Mide calidad y tiempo de M2 desde instancias pequenas hasta n=13,509.
 * Objetivo: <2% gap en n=10,000 en <60s.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M2ScalabilityTest {
    private val solverM2 = SolverM2()

    @Test
    @Order(1)
    fun m2_pcb442() = runInstance(PCB_442)

    @Test
    @Order(2)
    fun m2_d657() = runInstance(D_657)

    @Test
    @Order(3)
    fun m2_pr1002() = runInstance(PR_1002)

    @Test
    @Order(4)
    fun m2_d2103() = runInstance(D_2103)

    @Test
    @Order(5)
    fun m2_pcb3038() = runInstance(PCB_3038)

    @Test
    @Order(6)
    fun m2_rl5915() = runInstance(RL_5915)

    @Test
    @Order(7)
    fun m2_rl11849() = runInstance(RL_11849)

    @Test
    @Order(8)
    fun m2_usa13509() = runInstance(USA_13509)

    private fun runInstance(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solverM2.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        val timeSec = timeMs / 1000.0

        val viable =
            when {
                ratio < 1.02 && timeSec < 60 -> "SI"
                ratio < 1.05 && timeSec < 300 -> "MARGINAL"
                else -> "NO"
            }

        println(
            "M2_SCALE | %-12s | n=%6d | ratio=%8.4fx | time=%9.1fs | viable=%s".format(
                instance.name,
                n,
                ratio,
                timeSec,
                viable,
            ),
        )
    }
}
