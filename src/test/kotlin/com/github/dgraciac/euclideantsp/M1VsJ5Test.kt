package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E052 — Comparacion SolverM1 (sub-cubico O(n^2 log n)) vs SolverJ5 (O(n^3)).
 *
 * Mide la perdida de calidad (si la hay) de M1 frente a J5 en instancias conocidas,
 * y luego escala M1 a instancias grandes donde J5 es inviable.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M1VsJ5Test {
    private val solverJ5 = SolverJ5()
    private val solverM1 = SolverM1()

    // Instancias pequeñas/medianas: comparar J5 vs M1 en calidad y tiempo
    @Test
    @Order(1)
    fun compare_eil51() = compareOnInstance(EIL_51)

    @Test
    @Order(2)
    fun compare_berlin52() = compareOnInstance(BERLIN_52)

    @Test
    @Order(3)
    fun compare_st70() = compareOnInstance(ST_70)

    @Test
    @Order(4)
    fun compare_eil76() = compareOnInstance(EIL_76)

    @Test
    @Order(5)
    fun compare_rat99() = compareOnInstance(RAT_99)

    @Test
    @Order(6)
    fun compare_kro200() = compareOnInstance(KRO_200)

    @Test
    @Order(7)
    fun compare_a280() = compareOnInstance(A_280)

    @Test
    @Order(8)
    fun compare_pcb442() = compareOnInstance(PCB_442)

    @Test
    @Order(9)
    fun compare_d657() = compareOnInstance(D_657)

    @Test
    @Order(10)
    fun compare_pr1002() = compareOnInstance(PR_1002)

    @Test
    @Order(11)
    fun compare_d2103() = compareOnInstance(D_2103)

    // Instancias grandes: solo M1 (J5 es inviable)
    @Test
    @Order(20)
    fun scale_m1_pcb3038() = scaleM1(PCB_3038)

    @Test
    @Order(21)
    fun scale_m1_rl5915() = scaleM1(RL_5915)

    @Test
    @Order(22)
    fun scale_m1_rl11849() = scaleM1(RL_11849)

    @Test
    @Order(23)
    fun scale_m1_usa13509() = scaleM1(USA_13509)

    private fun compareOnInstance(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size

        var j5Tour: Tour? = null
        val j5Time = measureTimeMillis { j5Tour = solverJ5.compute(instance) }
        val j5Ratio = j5Tour!!.length / instance.optimalLength

        var m1Tour: Tour? = null
        val m1Time = measureTimeMillis { m1Tour = solverM1.compute(instance) }
        val m1Ratio = m1Tour!!.length / instance.optimalLength

        val speedup = j5Time.toDouble() / m1Time
        val qualityDiff = m1Ratio - j5Ratio

        println(
            "COMPARE | %-12s | n=%5d | J5=%7.4fx %7.1fs | M1=%7.4fx %7.1fs | speedup=%5.2fx | quality_diff=%+.4f".format(
                instance.name,
                n,
                j5Ratio,
                j5Time / 1000.0,
                m1Ratio,
                m1Time / 1000.0,
                speedup,
                qualityDiff,
            ),
        )
    }

    private fun scaleM1(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solverM1.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        val timeSec = timeMs / 1000.0

        val viable =
            when {
                ratio < 1.02 && timeSec < 60 -> "SI"
                ratio < 1.05 && timeSec < 300 -> "MARGINAL"
                else -> "NO"
            }

        println(
            "SCALE_M1 | %-12s | n=%6d | ratio=%8.4fx | time=%9.1fs | viable=%s".format(
                instance.name,
                n,
                ratio,
                timeSec,
                viable,
            ),
        )
    }
}
