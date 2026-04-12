package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL2 — SolverJ5 con DB rapido en dos fases
 *
 * Linea de investigacion: L (solver unico optimo en todas las escalas)
 * Padre: SolverJ5
 * Experimento: E041
 *
 * Hipotesis: Reemplazar el DB pesado de J5 por el DB en dos fases (evaluacion rapida
 * + refinamiento profundo selectivo) reduce el tiempo ~4x sin perder calidad.
 * El tiempo ahorrado puede usarse para mas multi-starts.
 *
 * Algoritmo: Igual que SolverJ5 pero con doubleBridgeFast (50 rapidos + 5 profundos)
 * en vez de doubleBridgePerturbation (20 todos profundos).
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.015, tiempo=0.043s
 *   berlin52: ratio=1.000, tiempo=0.085s
 *   st70:     ratio=1.011, tiempo=0.166s
 *   eil76:    ratio=1.031, tiempo=0.053s
 *   rat99:    ratio=1.008, tiempo=0.061s
 *   kro200:   ratio=1.004, tiempo=0.499s
 *   a280:     ratio=1.006, tiempo=0.336s
 *   pcb442:   ratio=1.017, tiempo=1.819s
 *   d657:     ratio=1.037, tiempo=11.526s
 *   rat783:   ratio=1.031, tiempo=13.986s
 *   pr1002:   ratio=1.026, tiempo=23.902s
 *   d1291:    ratio=1.047, tiempo=121.322s
 *   d2103:    ratio=1.014, tiempo=194.791s
 *
 * Metricas agregadas: Media aritmetica=1.019x | Media geometrica=1.018x | Peor caso=1.047x
 *
 * Conclusion: DB en dos fases (50 rapidos + 5 profundos) reduce tiempo 1.2-6.6x con calidad
 * casi identica a J5. Pierde ligeramente en eil51 y pcb442, gana en st70 y d1291. Es una
 * mejora de eficiencia sin degradar la aproximacion de forma significativa.
 */
class SolverL2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start (hull + construcciones) — igual que J5
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

        // Pipeline profundo
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Rama A: LK-2 + DB rapido + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeFast(afterLk2, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5) + DB rapido + LK-deep(5)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeFast(afterLkDeep, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
