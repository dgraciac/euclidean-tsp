package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Double-bridge en dos fases: evaluacion rapida + refinamiento profundo.
 *
 * Fase 1 (rapida): prueba muchas perturbaciones con solo 2-opt-nl (O(n^2) cada una).
 * Fase 2 (profunda): re-optimiza solo las mejores con twoOpt + orOpt (O(n^3) cada una).
 *
 * Esto es ~4x mas rapido que doubleBridgePerturbation porque evita re-optimizar
 * perturbaciones que no son prometedoras.
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param neighborLists candidatos para 2-opt-nl
 * @param quickAttempts numero de perturbaciones en fase rapida
 * @param deepAttempts numero de mejores perturbaciones a refinar en fase profunda
 * @return mejor tour encontrado
 *
 * Complejidad peor caso: O(quickAttempts * n^2 + deepAttempts * n^3)
 * Con constantes por defecto: O(50 * n^2 + 5 * n^3) = O(n^3)
 */
fun doubleBridgeFast(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    quickAttempts: Int = 50,
    deepAttempts: Int = 5,
): List<Point> {
    val points = tourPoints.dropLast(1)
    val n = points.size
    if (n < 8) return tourPoints

    val originalLength = points.indices.sumOf { points[it].distance(points[(it + 1) % n]) }

    // Candidatos: top 12 aristas mas largas
    val candidates =
        (0 until n)
            .map { i -> Pair(i, points[i].distance(points[(i + 1) % n])) }
            .sortedByDescending { it.second }
            .take(minOf(12, n))
            .map { it.first }
            .sorted()

    // Fase 1: evaluacion rapida con 2-opt-nl
    data class QuickResult(
        val tour: List<Point>,
        val length: Double,
    )

    val quickResults = mutableListOf<QuickResult>()
    var attempts = 0

    for (a in candidates.indices) {
        if (attempts >= quickAttempts) break
        for (b in a + 1 until candidates.size) {
            if (attempts >= quickAttempts) break
            for (c in b + 1 until candidates.size) {
                if (attempts >= quickAttempts) break

                val i1 = candidates[a]
                val i2 = candidates[b]
                val i3 = candidates[c]
                if (i2 - i1 < 1 || i3 - i2 < 1 || n - i3 < 1) continue

                val perturbed =
                    (
                        (0..i1).map { points[it] } +
                            (i2 + 1..i3).map { points[it] } +
                            (i1 + 1..i2).map { points[it] } +
                            (i3 + 1 until n).map { points[it] }
                    ).toMutableList()
                perturbed.add(perturbed.first())

                // Evaluacion rapida: solo 2-opt-nl
                val quick = twoOptWithNeighborLists(perturbed, neighborLists)
                val quickLen =
                    quick.dropLast(1).let { pts ->
                        pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) }
                    }

                quickResults.add(QuickResult(quick, quickLen))
                attempts++
            }
        }
    }

    // Fase 2: refinar las top deepAttempts mejores con pipeline completo
    val topCandidates = quickResults.sortedBy { it.length }.take(deepAttempts)

    var bestTour = tourPoints
    var bestLength = originalLength

    for (candidate in topCandidates) {
        // Solo refinar si la evaluacion rapida es prometedora
        if (candidate.length > originalLength * 1.05) continue

        val afterTwoOpt = twoOpt(candidate.tour)
        val afterOrOpt = orOpt(afterTwoOpt)
        val finalTour = twoOpt(afterOrOpt)
        val finalLength =
            finalTour.dropLast(1).let { pts ->
                pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) }
            }

        if (finalLength < bestLength - 1e-10) {
            bestTour = finalTour
            bestLength = finalLength
        }
    }

    return bestTour
}
