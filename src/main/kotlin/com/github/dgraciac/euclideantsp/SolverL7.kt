package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL7 — SolverL2 con DistanceMatrix en todo el pipeline
 *
 * Linea de investigacion: L (eficiencia sin sacrificar calidad)
 * Padre: SolverL2
 * Experimento: E047
 *
 * Hipotesis: Precalcular la matriz de distancias y pasarla a TODAS las funciones
 * del pipeline (twoOpt, orOpt, LK, DB, construcciones, 2-opt-nl) elimina el overhead
 * de JTS en cada llamada a Point.distance(). Los resultados deben ser IDENTICOS a L2
 * (mismas distancias, mismas decisiones). Solo los tiempos cambian.
 *
 * Algoritmo: Identico a L2 pero con dm pasado a todas las funciones.
 *
 * Complejidad e2e: O(n^3) — igual que L2
 * Complejidad peor caso: O(n^3) + O(n^2) para construir la matriz
 *
 * Resultados: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverL7 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Precalcular matriz de distancias — O(n^2), se amortiza en todo el pipeline
        val dm = DistanceMatrix(instance.points.toList())

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start (hull + construcciones) — con dm
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
            val nnTour = nearestNeighborFrom(instance.points, startPoint, dm)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl, dm)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        val constructions =
            listOf(
                farthestInsertion(instance.points, dm),
                convexHullInsertion(instance.points, dm),
                peelingInsertion(instance.points, dm),
                greedyConstruction(instance.points, dm),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, combinedNl, dm)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Pipeline profundo — con dm
        val afterOrOpt = orOpt(bestTour!!.points, dm = dm)
        val afterTwoOpt = twoOpt(afterOrOpt, dm)

        // Rama A: LK-2 + DB rapido + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl, dm)
        val afterDb2 = doubleBridgeFast(afterLk2, combinedNl, quickAttempts = 50, deepAttempts = 5, dm = dm)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl, dm))

        // Rama B: LK-deep(5) + DB rapido + LK-deep(5)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5, dm = dm)
        val afterDbDeep = doubleBridgeFast(afterLkDeep, combinedNl, quickAttempts = 50, deepAttempts = 5, dm = dm)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5, dm = dm))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
