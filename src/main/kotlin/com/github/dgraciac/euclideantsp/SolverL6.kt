package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL6 — SolverL2 con or-opt restringido a neighbor lists
 *
 * Linea de investigacion: L (eficiencia sin sacrificar calidad)
 * Padre: SolverL2
 * Experimento: E046
 *
 * Hipotesis: Or-opt actual es O(n^2) por pasada (prueba todas las posiciones).
 * Restringir a neighbor lists lo hace O(n*K) por pasada. Or-opt es ~2% del tiempo
 * de L2, asi que la mejora esperada es pequeña. Verificar que la calidad no baje.
 *
 * Complejidad e2e: O(n^3) — or-opt pasa de O(n^3) a O(n^2*K) = O(n^2), no cambia total
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverL6 : Euclidean2DTSPSolver {
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

        // Or-opt con neighbor lists en vez de or-opt completo
        val afterOrOpt = orOptWithNeighborLists(bestTour!!.points, combinedNl)
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
