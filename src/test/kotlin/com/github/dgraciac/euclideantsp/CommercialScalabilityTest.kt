package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * Fase 0.1 del plan de comercializacion — Evaluar escalabilidad del solver a escala industrial.
 *
 * Ejecuta SolverJ5 (mejor solver O(n^3)) en instancias TSPLIB de n=442 a n=13,509
 * para medir calidad (gap vs optimo) y tiempo de ejecucion a escala industrial.
 *
 * Criterio de exito: <2% gap en n=10,000 en <60s.
 *
 * Cada instancia es un test separado para que un OOM o timeout no bloquee las demas.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class CommercialScalabilityTest {
    private val solverJ5 = SolverJ5()

    @Test
    @Order(1)
    fun scale_0442_pcb442() = runInstance(PCB_442)

    @Test
    @Order(2)
    fun scale_0657_d657() = runInstance(D_657)

    @Test
    @Order(3)
    fun scale_1002_pr1002() = runInstance(PR_1002)

    @Test
    @Order(4)
    fun scale_2103_d2103() = runInstance(D_2103)

    @Test
    @Order(5)
    fun scale_3038_pcb3038() = runInstance(PCB_3038)

    @Test
    @Order(6)
    fun scale_5915_rl5915() = runInstance(RL_5915)

    @Test
    @Order(7)
    fun scale_11849_rl11849() = runInstance(RL_11849)

    @Test
    @Order(8)
    fun scale_13509_usa13509() = runInstance(USA_13509)

    private fun runInstance(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solverJ5.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        val timeSec = timeMs / 1000.0

        val viable =
            when {
                ratio < 1.02 && timeSec < 60 -> "SI"
                ratio < 1.05 && timeSec < 300 -> "MARGINAL"
                else -> "NO"
            }

        println(
            "SCALABILITY | %-12s | n=%6d | ratio=%8.4fx | time=%9.1fs | viable=%s".format(
                instance.name,
                n,
                ratio,
                timeSec,
                viable,
            ),
        )
    }
}
