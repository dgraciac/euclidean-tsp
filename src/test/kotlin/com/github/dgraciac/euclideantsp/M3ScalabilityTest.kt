package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E054 — SolverM3: escalabilidad agresiva sin alpha-nearness.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M3ScalabilityTest {
    private val solverM3 = SolverM3()

    @Test
    @Order(1)
    fun m3_pcb442() = runInstance(PCB_442)

    @Test
    @Order(2)
    fun m3_pr1002() = runInstance(PR_1002)

    @Test
    @Order(3)
    fun m3_d2103() = runInstance(D_2103)

    @Test
    @Order(4)
    fun m3_pcb3038() = runInstance(PCB_3038)

    @Test
    @Order(5)
    fun m3_rl5915() = runInstance(RL_5915)

    @Test
    @Order(6)
    fun m3_rl11849() = runInstance(RL_11849)

    @Test
    @Order(7)
    fun m3_usa13509() = runInstance(USA_13509)

    private fun runInstance(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solverM3.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        val timeSec = timeMs / 1000.0

        val viable =
            when {
                ratio < 1.02 && timeSec < 60 -> "SI"
                ratio < 1.05 && timeSec < 300 -> "MARGINAL"
                else -> "NO"
            }

        println(
            "M3_SCALE | %-12s | n=%6d | ratio=%8.4fx | time=%9.1fs | viable=%s".format(
                instance.name,
                n,
                ratio,
                timeSec,
                viable,
            ),
        )
    }
}
