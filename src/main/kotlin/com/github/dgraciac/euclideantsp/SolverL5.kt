package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL5 — SolverL2 con K=5+5 candidatos (en vez de K=7+7)
 *
 * Linea de investigacion: L (eficiencia sin sacrificar calidad)
 * Padre: SolverL2
 * Experimento: E045
 *
 * Hipotesis: Reducir candidatos de K=7+7 (~14 por punto) a K=5+5 (~10 por punto)
 * acelera 2-opt-nl y LK (menos candidatos a evaluar) sin perder calidad significativa.
 * E029 mostro inestabilidad con K=5+5 en SolverJ2, pero el pipeline mejorado de L2
 * (DB dos fases + LK) podria compensar.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverL5 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // K=5+5 en vez de K=7+7
        val alphaNl = buildAlphaNearnessList(instance.points, k = 5)
        val distNl = buildNeighborLists(instance.points, k = 5)
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
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
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
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeFast(afterLk2, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeFast(afterLkDeep, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
