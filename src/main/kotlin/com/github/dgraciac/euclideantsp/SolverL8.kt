package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL8 — L3 (sin LK-deep) + mas intentos de DB profundos
 *
 * Linea de investigacion: L (mejorar calidad sin perder rapidez)
 * Padre: SolverL3
 * Experimento: E048
 *
 * Hipotesis: L3 ahorra ~60% del tiempo al no usar LK-deep. Reinvertir ese tiempo
 * en mas intentos de DB profundos (DB pesado con 40 intentos en vez de DB-fast con
 * 50 rapidos + 5 profundos) podria mejorar la calidad manteniendo tiempo similar a L2.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 * Conclusion: PENDIENTE
 */
class SolverL8 : Euclidean2DTSPSolver {
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

        // Pipeline: or-opt + 2-opt + LK(2) + DB PESADO (40 intentos) + LK(2)
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, combinedNl)
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 40)
        val finalTour = linKernighan(afterDb, combinedNl)

        return Tour(points = finalTour)
    }
}
