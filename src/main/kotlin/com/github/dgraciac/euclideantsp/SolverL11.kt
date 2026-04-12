package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL11 — L2 con DB α-nearness REEMPLAZANDO al DB estandar (no ambos)
 *
 * Linea de investigacion: L (mejorar calidad sin perder rapidez)
 * Padre: SolverL9
 * Experimento: E051
 *
 * Hipotesis: L9 mejoraba calidad pero tardaba 2x porque ejecutaba AMBOS DBs.
 * Si reemplazamos el DB estandar (aristas largas) por el DB α-nearness (aristas
 * fuera de lugar en MST), obtenemos la mejora de calidad de L9 sin el coste extra.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 * Conclusion: PENDIENTE
 */
class SolverL11 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        val hull =
            ConvexHull(
                instance.points.map { it.toCoordinate() }.toTypedArray(),
                GeometryFactory(),
            ).convexHull
        val startPoints =
            hull.coordinates.dropLast(1).map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        var bestTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) bestTour = tour
        }

        val constructions =
            listOf(
                farthestInsertion(instance.points),
                convexHullInsertion(instance.points),
                peelingInsertion(instance.points),
                greedyConstruction(instance.points),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) bestTour = tour
        }

        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Rama A: LK-2 + DB α-nearness (REEMPLAZA DB estandar) + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeAlphaOnly(afterLk2, combinedNl)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5) + DB α-nearness + LK-deep(5)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeAlphaOnly(afterLkDeep, combinedNl)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }

    /**
     * DB que corta SOLO en aristas con mayor "alpha gap" (ratio distancia/vecino cercano).
     * Reemplaza doubleBridgeFast, no lo complementa.
     * Usa DB-fast dos fases (50 rapidos + 5 profundos) pero con candidatos α.
     */
    private fun doubleBridgeAlphaOnly(
        tourPoints: List<Point>,
        neighborLists: Map<Point, List<Point>>,
    ): List<Point> {
        val points = tourPoints.dropLast(1)
        val n = points.size
        if (n < 8) return tourPoints

        // Candidatos: aristas con mayor ratio distancia/vecino_cercano
        val edgeScores =
            (0 until n)
                .map { i ->
                    val a = points[i]
                    val b = points[(i + 1) % n]
                    val edgeDist = a.distance(b)
                    val bestNeighborDist =
                        (neighborLists[a] ?: emptyList())
                            .filter { it != b }
                            .minOfOrNull { a.distance(it) } ?: edgeDist
                    val gap = edgeDist / maxOf(bestNeighborDist, 1e-10)
                    Pair(i, gap)
                }.sortedByDescending { it.second }

        val candidates = edgeScores.take(minOf(12, n)).map { it.first }.sorted()

        // DB-fast dos fases con candidatos α
        var bestTour = tourPoints
        var bestLength = points.indices.sumOf { points[it].distance(points[(it + 1) % n]) }

        data class QuickResult(
            val tour: List<Point>,
            val length: Double,
        )

        val quickResults = mutableListOf<QuickResult>()
        var attempts = 0

        for (a in candidates.indices) {
            if (attempts >= 50) break
            for (b in a + 1 until candidates.size) {
                if (attempts >= 50) break
                for (c in b + 1 until candidates.size) {
                    if (attempts >= 50) break
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

        // Fase profunda: refinar top 5
        val topCandidates = quickResults.sortedBy { it.length }.take(5)
        for (candidate in topCandidates) {
            if (candidate.length > bestLength * 1.05) continue
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
}
