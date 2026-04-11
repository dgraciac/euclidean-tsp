package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ4 — SolverJ3 + LK profundidad 5 con backtracking
 *
 * Linea de investigacion: J (tecnicas LKH)
 * Padre: SolverJ3
 * Experimento: E030
 *
 * Hipotesis: LK con profundidad 5 y backtracking encuentra movimientos que LK
 * profundidad 2 no puede (incluyendo double-bridge integrado en la busqueda).
 * Combinado con α-nearness candidates, deberia acercarse significativamente a LKH.
 *
 * Algoritmo:
 * 1. α(7)+dist(7) candidates — O(n^2 log n)
 * 2. Multi-start(hull) + construcciones diversas + 2-opt-nl — O(n^2.5)
 * 3. Or-opt + 2-opt + LK-deep(5) — O(n^3) (dominante)
 * 4. Double-bridge(20) + LK-deep(5) — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — LK-deep tiene K^5 constante por mejora, O(n) mejoras
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.028s
 *   berlin52: ratio=1.000, tiempo=0.021s
 *   st70:     ratio=1.020, tiempo=0.035s
 *   eil76:    ratio=1.025, tiempo=0.032s
 *   rat99:    ratio=1.007, tiempo=0.065s
 *   kro200:   ratio=1.004, tiempo=0.268s
 *   a280:     ratio=1.006, tiempo=0.606s
 *   pcb442:   ratio=1.016, tiempo=2.108s
 *
 * Metricas agregadas: Media aritmetica=1.011x | Media geometrica=1.010x | Peor caso=1.025x
 *
 * Conclusion: LK profundidad 5 mejora en kro200 (1.004x vs 1.012x de J3) pero empeora en pcb442
 * (1.016x vs 1.013x). El backtracking encuentra mejores movimientos en instancias medianas, pero la
 * reconstruccion de tours con 5+ segmentos tiene reconexiones limitadas. Media similar a SolverJ3
 * (1.010x vs 1.011x). Pendiente: mejorar reconexiones para segmentos profundos.
 */
class SolverJ4 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start rapido
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
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

        // Busqueda profunda con LK-deep(5)
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)

        // Double-bridge + LK-deep final
        val afterDb = doubleBridgePerturbation(afterLkDeep, maxAttempts = 20)
        val finalTour = linKernighanDeep(afterDb, combinedNl, maxDepth = 5)

        return Tour(points = finalTour)
    }
}
