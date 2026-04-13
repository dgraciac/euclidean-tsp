package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * E056-E059 — Comparacion M4/M5/M6/M7 vs M2 (referencia).
 *
 * Ejecuta M2, M4, M5, M6, M7 secuencialmente en cada instancia para comparar
 * calidad y velocidad con tiempos limpios.
 *
 * M4: LK-deep prof 3 (item 1)
 * M5: LK-deep K=7 alpha-only (item 2)
 * M6: sin LK-deep post-DB (item 3)
 * M7: combina 1+2+3 (item 4)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class M4toM7Test {
    private val solvers =
        listOf(
            "M2" to SolverM2(),
            "M4" to SolverM4(),
            "M5" to SolverM5(),
            "M6" to SolverM6(),
            "M7" to SolverM7(),
        )

    @Test
    @Order(1)
    fun compare_eil51() = compareAll(EIL_51)

    @Test
    @Order(2)
    fun compare_berlin52() = compareAll(BERLIN_52)

    @Test
    @Order(3)
    fun compare_st70() = compareAll(ST_70)

    @Test
    @Order(4)
    fun compare_eil76() = compareAll(EIL_76)

    @Test
    @Order(5)
    fun compare_rat99() = compareAll(RAT_99)

    @Test
    @Order(6)
    fun compare_kro200() = compareAll(KRO_200)

    @Test
    @Order(7)
    fun compare_a280() = compareAll(A_280)

    @Test
    @Order(8)
    fun compare_pcb442() = compareAll(PCB_442)

    @Test
    @Order(9)
    fun compare_d657() = compareAll(D_657)

    @Test
    @Order(10)
    fun compare_pr1002() = compareAll(PR_1002)

    @Test
    @Order(11)
    fun compare_d2103() = compareAll(D_2103)

    @Test
    @Order(12)
    fun compare_pcb3038() = compareAll(PCB_3038)

    private fun compareAll(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        val results = mutableListOf<String>()

        for ((name, solver) in solvers) {
            var tour: Tour? = null
            val timeMs = measureTimeMillis { tour = solver.compute(instance) }
            val ratio = tour!!.length / instance.optimalLength
            results.add(
                "%s=%7.4fx/%6.1fs".format(name, ratio, timeMs / 1000.0),
            )
        }

        println(
            "CMP | %-12s | n=%5d | %s".format(
                instance.name,
                n,
                results.joinToString(" | "),
            ),
        )
    }
}
