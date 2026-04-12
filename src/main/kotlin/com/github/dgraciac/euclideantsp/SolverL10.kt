package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL10 — L2 con LK final usando K=20 candidatos (en vez de K=14)
 *
 * Linea de investigacion: L (mejorar calidad sin perder rapidez)
 * Padre: SolverL2
 * Experimento: E050
 *
 * Hipotesis: Aumentar K solo para la ultima pasada de LK (post-DB) permite
 * encontrar movimientos que K=14 no ve. Las fases anteriores (multi-start, or-opt,
 * 2-opt) siguen con K=14 para no ralentizarlas. Solo el LK final es mas caro
 * pero potencialmente encuentra mejoras adicionales.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — K=20 sigue siendo constante
 *
 * Resultados: PENDIENTE
 * Conclusion: PENDIENTE
 */
class SolverL10 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Candidatos standard (K=7+7) para fases rapidas
        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Candidatos amplios (K=10+10) para LK final
        val alphaNlWide = buildAlphaNearnessList(instance.points, k = 10)
        val distNlWide = buildNeighborLists(instance.points, k = 10)
        val wideNl =
            instance.points.associateWith { p ->
                ((alphaNlWide[p] ?: emptyList()) + (distNlWide[p] ?: emptyList())).distinct()
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

        // Rama A: LK-2 (K=14) + DB-fast + LK-2 con K=20 (final amplio)
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeFast(afterLk2, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourA = Tour(points = linKernighan(afterDb2, wideNl)) // <-- K=20 aqui

        // Rama B: LK-deep(5) (K=14) + DB-fast + LK-deep(5) con K=20
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeFast(afterLkDeep, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, wideNl, maxDepth = 5)) // <-- K=20 aqui

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
