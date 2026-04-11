package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverG1 — Multi-construccion diversa + multi-start NN(hull) + local search
 *
 * Linea de investigacion: G (diversidad de construccion)
 * Experimento: E019
 *
 * Hipotesis: Diferentes heuristicas de construccion producen tours estructuralmente
 * distintos. Al combinar multi-start NN desde el hull con construcciones diversas
 * (farthest insertion, peeling, greedy, convex hull insertion), obtenemos mas
 * diversidad de optimos locales que usando solo NN, mejorando la calidad sin
 * aumentar mucho el coste.
 *
 * Algoritmo:
 * 1. Multi-start NN desde vertices del hull (h tours) — O(h * n^3)
 * 2. Farthest insertion + local search (1 tour) — O(n^3)
 * 3. Convex hull insertion + local search (1 tour) — O(n^3)
 * 4. Peeling insertion + local search (1 tour) — O(n^3)
 * 5. Greedy construction + local search (1 tour) — O(n^3)
 * Retornar el mejor de todos.
 *
 * Complejidad e2e: O((h + 4) * n^3) ≈ O(n^3.5)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.013s
 *   berlin52: ratio=1.000, tiempo=0.010s
 *   st70:     ratio=1.016, tiempo=0.022s
 *   eil76:    ratio=1.034, tiempo=0.027s
 *   rat99:    ratio=1.008, tiempo=0.050s
 *   kro200:   ratio=1.023, tiempo=0.219s
 *   a280:     ratio=1.020, tiempo=0.556s
 *   pcb442:   ratio=PENDIENTE (test interrumpido)
 *
 * Metricas agregadas (sin pcb442): Media aritmetica=1.015x | Media geometrica=1.015x | Peor caso=1.034x
 *
 * Conclusion: La diversidad de construccion ayuda en instancias grandes (rat99: 1.008x,
 * a280: 1.020x — mejor que SolverE3 que da 1.016x y 1.029x). Farthest insertion y greedy
 * construction producen optimos locales distintos a NN. Buen tradeoff: O(n^3.5) y <0.6s.
 */
class SolverG1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val constructions = mutableListOf<List<Point>>()

        // Multi-start NN desde vertices del hull
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }
        for (startPoint in startPoints) {
            constructions.add(nearestNeighborFrom(instance.points, startPoint))
        }

        // Construcciones diversas
        constructions.add(farthestInsertion(instance.points))
        constructions.add(convexHullInsertion(instance.points))
        constructions.add(peelingInsertion(instance.points))
        constructions.add(greedyConstruction(instance.points))

        // Aplicar local search a cada construccion y quedarse con el mejor
        var bestTour: Tour? = null
        for (construction in constructions) {
            val afterTwoOpt = twoOpt(construction)
            val afterOrOpt = orOpt(afterTwoOpt)
            val finalPoints = twoOpt(afterOrOpt)
            val tour = Tour(points = finalPoints)

            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        return bestTour!!
    }
}
