package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import kotlin.system.measureTimeMillis

internal class ComparisonTest {
    companion object {
        val solvers: List<Pair<String, Euclidean2DTSPSolver>> =
            listOf(
                // === Solvers activos: solo los mejores por categoria ===
                // Descomentar otros para comparacion completa (aumenta tiempo de test)
                "Christofides" to Christofides(), // Baseline O(n^3), garantia 3/2
                "SolverJ5" to SolverJ5(), // Mejor O(n^3): media 1.010x
                // "SolverH3" to SolverH3(), // O(n^4) — DEMASIADO LENTO para d657/rat783 (>1h)
                // "SolverJ7" to SolverJ7(), // E036: LK no secuencial — identico a J5
                // === Solvers anteriores (superados, descomentar si se necesitan) ===
                // "SolverJ6" to SolverJ6(), // Subgradient, no mejora sobre J5 (E032)
                // "SolverJ4" to SolverJ4(), // LK-deep(5), inestable en pcb442 (E030)
                // "SolverJ3" to SolverJ3(), // α-nearness + LK-2, superado por J5
                // "SolverJ2" to SolverJ2(), // α5+dist5, inestable (E029)
                // "SolverJ1" to SolverJ1(), // α solo, inestable en a280 (E029)
                // "SolverI2" to SolverI2(), // Mejor O(n^3) anterior, superado por J5
                // "SolverI1" to SolverI1(), // O(n^3) sin DB, superado por I2
                // "SolverH2" to SolverH2(), // Multi-start selectivo + LK + DB
                // "SolverH1" to SolverH1(), // Multi-start selectivo + DB
                // "SolverG2" to SolverG2(), // Multi-start completo + 2-opt-nl + diversidad
                // "SolverG1" to SolverG1(), // Multi-start selectivo + diversidad
                // "SolverE7" to SolverE7(), // Multi-start + 2-opt-nl
                // "SolverE3" to SolverE3(), // Multi-start selectivo (hull)
                // "SolverE2" to SolverE2(), // Multi-start completo
                // "SolverC3" to SolverC3(), // Peeling + 2-opt + or-opt
            )
    }

    @ParameterizedTest
    @ArgumentsSource(TSPInstanceProvider::class)
    fun solve_instances(instance: Euclidean2DTSPInstance) {
        println("Instance name: ${instance.name}")

        solvers.forEach { (name, solver) ->
            var tour: Tour? = null
            val timeMs =
                measureTimeMillis {
                    tour = solver.compute(instance)
                }
            val tourLength = tour?.length
            val approximation = tourLength?.div(instance.optimalLength)
            println(
                "Algorithm: $name; Time: ${timeMs / 1000.0}s; Length: $tourLength; Approximation: $approximation",
            )
        }

        println("=====================================================================================================")
    }
}
