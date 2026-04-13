package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.system.measureTimeMillis

/**
 * Rellenar huecos de mediciones para M2 y M3 en instancias donde no se midieron.
 * Ejecutar secuencialmente: primero M2, luego M3.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class FillGapsTest {
    private val solverM2 = SolverM2()
    private val solverM3 = SolverM3()

    // M2 gaps: eil51 to a280, d657, d2103
    @Test
    @Order(1)
    fun m2_eil51() = run("M2", solverM2, EIL_51)

    @Test
    @Order(2)
    fun m2_berlin52() = run("M2", solverM2, BERLIN_52)

    @Test
    @Order(3)
    fun m2_st70() = run("M2", solverM2, ST_70)

    @Test
    @Order(4)
    fun m2_eil76() = run("M2", solverM2, EIL_76)

    @Test
    @Order(5)
    fun m2_rat99() = run("M2", solverM2, RAT_99)

    @Test
    @Order(6)
    fun m2_kro200() = run("M2", solverM2, KRO_200)

    @Test
    @Order(7)
    fun m2_a280() = run("M2", solverM2, A_280)

    @Test
    @Order(8)
    fun m2_d657() = run("M2", solverM2, D_657)

    @Test
    @Order(9)
    fun m2_d2103() = run("M2", solverM2, D_2103)

    // M3 gaps: eil51 to a280, d657
    @Test
    @Order(20)
    fun m3_eil51() = run("M3", solverM3, EIL_51)

    @Test
    @Order(21)
    fun m3_berlin52() = run("M3", solverM3, BERLIN_52)

    @Test
    @Order(22)
    fun m3_st70() = run("M3", solverM3, ST_70)

    @Test
    @Order(23)
    fun m3_eil76() = run("M3", solverM3, EIL_76)

    @Test
    @Order(24)
    fun m3_rat99() = run("M3", solverM3, RAT_99)

    @Test
    @Order(25)
    fun m3_kro200() = run("M3", solverM3, KRO_200)

    @Test
    @Order(26)
    fun m3_a280() = run("M3", solverM3, A_280)

    @Test
    @Order(27)
    fun m3_d657() = run("M3", solverM3, D_657)

    private fun run(
        name: String,
        solver: com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver,
        instance: Euclidean2DTSPInstance,
    ) {
        var tour: Tour? = null
        val timeMs = measureTimeMillis { tour = solver.compute(instance) }
        val ratio = tour!!.length / instance.optimalLength
        println(
            "FILL | %-3s | %-12s | n=%5d | ratio=%8.4fx | time=%9.1fs".format(
                name,
                instance.name,
                instance.points.size,
                ratio,
                timeMs / 1000.0,
            ),
        )
    }
}
