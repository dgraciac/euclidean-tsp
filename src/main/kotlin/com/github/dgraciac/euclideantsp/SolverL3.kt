package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL3 — SolverL2 sin Rama B (LK-deep), solo Rama A (LK-2)
 *
 * Linea de investigacion: L (eficiencia sin sacrificar calidad)
 * Padre: SolverL2
 * Experimento: E043
 *
 * Hipotesis: La Rama B (LK-deep profundidad 5) consume ~60% del tiempo de L2
 * pero rara vez gana sobre la Rama A (LK-2). Eliminarla reduce el tiempo ~2x
 * sin perder calidad en la mayoria de instancias.
 *
 * Algoritmo:
 * 1. α(7)+dist(7) candidates — O(n^2 log n)
 * 2. Multi-start(hull) + construcciones + 2-opt-nl — O(n^2.5)
 * 3. Or-opt + 2-opt — O(n^3)
 * 4. LK(2) + DB-fast(50 rapidos + 5 profundos) + LK(2) — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: 1.5-7x mas rapido que L2. Calidad identica en 10/15 instancias.
 *   Pierde 0.2-1.5% en 5 instancias (kro200, pr1002, u1060, fl1577, d2103).
 *   d2103: 61.5s vs 199.7s (3.3x), ratio 1.017x vs 1.014x.
 *
 * Metricas agregadas: Media ~1.019x (L2: ~1.018x). Peor caso 1.061x (L2: 1.047x).
 *
 * Conclusion: LK-deep(5) aporta ~0.3% de calidad media pero cuesta ~60% del tiempo.
 *   L3 es el solver mas eficiente del proyecto: ~3x mas rapido que L2 con calidad casi identica.
 *   Pero u1060 muestra que LK-deep importa en ciertas instancias (1.5% peor sin el).
 */
class SolverL3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start (hull + construcciones)
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

        // Pipeline: or-opt + 2-opt + LK(2) + DB-fast + LK(2)
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, combinedNl)
        val afterDb = doubleBridgeFast(afterLk, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val finalTour = linKernighan(afterDb, combinedNl)

        return Tour(points = finalTour)
    }
}
