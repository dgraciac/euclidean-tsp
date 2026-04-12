package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL9 — L2 con DB que corta en aristas con α alto (fuera de lugar en MST)
 *
 * Linea de investigacion: L (mejorar calidad sin perder rapidez)
 * Padre: SolverL2
 * Experimento: E049
 *
 * Hipotesis: El DB actual corta en las 12 aristas mas largas del tour. Pero una arista
 * larga puede ser correcta si conecta clusters lejanos. Mejor criterio: aristas con α
 * alto (lejos de estar en el MST), que son las mas probables de ser sub-optimas.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 * Conclusion: PENDIENTE
 */
class SolverL9 : Euclidean2DTSPSolver {
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

        // Rama A: LK-2 + DB con α-nearness + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeAlpha(afterLk2, instance.points, combinedNl)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5) + DB con α-nearness + LK-deep(5)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeAlpha(afterLkDeep, instance.points, combinedNl)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }

    /**
     * Double-bridge que corta en aristas con mayor "alpha gap" — diferencia entre
     * la distancia de la arista y lo que "deberia" costar segun el MST.
     * Usa la α-nearness de cada punto para evaluar si una arista esta "fuera de lugar".
     */
    private fun doubleBridgeAlpha(
        tourPoints: List<Point>,
        allPoints: Set<Point>,
        neighborLists: Map<Point, List<Point>>,
    ): List<Point> {
        val points = tourPoints.dropLast(1)
        val n = points.size
        if (n < 8) return tourPoints

        // Para cada arista del tour, calcular "alpha gap":
        // cuanto cuesta la arista comparado con la arista mas corta del vecindario
        val edgeScores =
            (0 until n)
                .map { i ->
                    val a = points[i]
                    val b = points[(i + 1) % n]
                    val edgeDist = a.distance(b)
                    // Distancia minima al vecino mas cercano de a (que no sea b)
                    val bestNeighborDist =
                        (neighborLists[a] ?: emptyList())
                            .filter { it != b }
                            .minOfOrNull { a.distance(it) } ?: edgeDist
                    val gap = edgeDist / maxOf(bestNeighborDist, 1e-10)
                    Pair(i, gap)
                }.sortedByDescending { it.second }

        val candidates = edgeScores.take(minOf(12, n)).map { it.first }.sorted()

        // DB-fast con estos candidatos
        return doubleBridgeFast(tourPoints, neighborLists, quickAttempts = 50, deepAttempts = 5)
            .let { standardResult ->
                // Tambien probar con los candidatos α
                val alphaResult = doubleBridgeWithCandidates(tourPoints, points, n, candidates)
                if (alphaResult != null) {
                    val stdLen = standardResult.dropLast(1).let { pts -> pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) } }
                    val alphaLen = alphaResult.dropLast(1).let { pts -> pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) } }
                    if (alphaLen < stdLen) alphaResult else standardResult
                } else {
                    standardResult
                }
            }
    }

    private fun doubleBridgeWithCandidates(
        tourPoints: List<Point>,
        points: List<Point>,
        n: Int,
        candidates: List<Int>,
    ): List<Point>? {
        var bestTour: List<Point>? = null
        var bestLength = points.indices.sumOf { points[it].distance(points[(it + 1) % n]) }

        var attempts = 0
        for (a in candidates.indices) {
            if (attempts >= 30) break
            for (b in a + 1 until candidates.size) {
                if (attempts >= 30) break
                for (c in b + 1 until candidates.size) {
                    if (attempts >= 30) break
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

                    val opt = twoOpt(orOpt(twoOpt(perturbed)))
                    val len = opt.dropLast(1).let { pts -> pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) } }
                    if (len < bestLength - 1e-10) {
                        bestTour = opt
                        bestLength = len
                    }
                    attempts++
                }
            }
        }
        return bestTour
    }
}
