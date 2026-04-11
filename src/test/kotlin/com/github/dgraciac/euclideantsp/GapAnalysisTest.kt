package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * E018 — Analisis del gap residual entre tours heuristicos y optimos.
 *
 * Compara arista por arista los tours producidos por nuestros mejores solvers
 * con el tour optimo (BruteForce) en instancias pequeñas.
 *
 * Objetivo: entender que tipo de estructura local diferencia un tour heuristico
 * del optimo, para guiar el diseño de mejores movimientos de busqueda local.
 */
internal class GapAnalysisTest {
    companion object {
        @JvmStatic
        fun analysisInstances(): Stream<Arguments> =
            Stream.of(
                Arguments.of(instance6A),
                Arguments.of(instance6B),
                Arguments.of(instance6C),
                Arguments.of(instance6D),
                Arguments.of(instance10A),
            )
    }

    @ParameterizedTest
    @MethodSource("analysisInstances")
    fun analyze_gap(instance: Euclidean2DTSPInstance) {
        val bruteForce = BruteForce()
        val optimalTour = bruteForce.compute(instance)
        val optimalEdges = tourToEdges(optimalTour)

        val solvers =
            listOf(
                "SolverE1" to SolverE1(),
                "SolverC3" to SolverC3(),
                "SolverB3" to SolverB3(),
            )

        println("=== Instance: ${instance.name} (${instance.points.size} points) ===")
        println("Optimal tour length: ${"%.4f".format(optimalTour.length)}")
        println("Optimal edges: ${optimalEdges.map { edgeStr(it) }}")
        println()

        for ((name, solver) in solvers) {
            val tour = solver.compute(instance)
            val heuristicEdges = tourToEdges(tour)

            val ratio = tour.length / instance.optimalLength
            val missingFromOptimal = optimalEdges - heuristicEdges
            val extraInHeuristic = heuristicEdges - optimalEdges

            println("$name: ratio=${"%.4f".format(ratio)}, length=${"%.4f".format(tour.length)}")

            if (missingFromOptimal.isEmpty()) {
                println("  PERFECT MATCH — tour es optimo")
            } else {
                println("  Aristas del optimo que faltan (${missingFromOptimal.size}):")
                for (edge in missingFromOptimal) {
                    println("    FALTA: ${edgeStr(edge)} (dist=${"%.2f".format(edge.first.distance(edge.second))})")
                }
                println("  Aristas extra del heuristico (${extraInHeuristic.size}):")
                for (edge in extraInHeuristic) {
                    println("    EXTRA: ${edgeStr(edge)} (dist=${"%.2f".format(edge.first.distance(edge.second))})")
                }

                // Analizar el tipo de diferencia
                val optLen =
                    missingFromOptimal.sumOf { it.first.distance(it.second) }
                val heurLen =
                    extraInHeuristic.sumOf { it.first.distance(it.second) }
                println("  Longitud aristas optimas faltantes: ${"%.4f".format(optLen)}")
                println("  Longitud aristas heuristicas extra: ${"%.4f".format(heurLen)}")
                println("  Gap por estas aristas: ${"%.4f".format(heurLen - optLen)}")
            }
            println()
        }
    }

    /**
     * Convierte un tour en un conjunto de aristas (pares de puntos sin orden).
     * Cada arista se normaliza para que el punto "menor" vaya primero.
     */
    private fun tourToEdges(tour: Tour): Set<Pair<Point, Point>> {
        val points = tour.points
        return (0 until points.size - 1)
            .map { i ->
                val a = points[i]
                val b = points[i + 1]
                // Normalizar: el punto con menor x (o menor y si x iguales) va primero
                if (a.x < b.x || (a.x == b.x && a.y < b.y)) Pair(a, b) else Pair(b, a)
            }.toSet()
    }

    private fun edgeStr(edge: Pair<Point, Point>): String = "(${edge.first.x},${edge.first.y})-(${edge.second.x},${edge.second.y})"
}
