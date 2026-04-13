package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM9 — M5 con KD-tree para buildNeighborLists
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM5 (unico solver sin perdida de calidad vs M2)
 * Experimento: E061
 *
 * Hipotesis: Reemplazar buildNeighborLists (O(n^2 log n)) por buildNeighborListsKdTree
 * (O(n K log n)) reduce el coste de infraestructura sin afectar calidad (los K vecinos
 * resultantes son identicos). Combinado con M5 (LK-deep K=7 alpha-only), M9 deberia
 * ser significativamente mas rapido que M2 en instancias grandes.
 *
 * Cambios respecto a M5:
 * - buildNeighborLists -> buildNeighborListsKdTree (O(n^2 log n) -> O(n K log n))
 * - LK-deep con alphaNl K=7 (de M5)
 * - Todo lo demas identico a M2
 *
 * Complejidad e2e: O(n^2) — dominada por alpha-nearness O(n^2). Si se implementa
 * item 6 (HLD para alpha), bajaria a O(n K log n) = O(n log n) con K constante.
 */
class SolverM9 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Alpha-nearness lean (O(n^2))
        val alphaNl = buildAlphaNearnessListLean(instance.points, k = 7)
        // Neighbor lists con KD-tree (O(n K log n) en vez de O(n^2 log n))
        val distNl = buildNeighborListsKdTree(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start con limite de hull points
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val allHullPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        val maxStartPoints = 20
        val startPoints =
            if (allHullPoints.size <= maxStartPoints) {
                allHullPoints
            } else {
                val step = allHullPoints.size.toDouble() / maxStartPoints
                (0 until maxStartPoints).map { allHullPoints[(it * step).toInt()] }
            }

        var baseTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
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
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        // Fase comun: or-opt-nl + 2-opt-nl
        val afterOrOpt = orOptWithNeighborLists(baseTour!!.points, combinedNl)
        val afterTwoOpt = twoOptWithNeighborLists(afterOrOpt, combinedNl)

        // Rama A: LK-2 + DB-nl + LK-2 (usa combinedNl)
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5, alphaNl K=7) + DB-nl + LK-deep(5, alphaNl K=7)
        val afterLk5 = linKernighanDeep(afterTwoOpt, alphaNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, alphaNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
