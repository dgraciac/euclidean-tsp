package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverE6 — Multi-start selectivo + 2-opt + or-opt + 3-opt
 *
 * Linea de investigacion: E
 * Padre: SolverE3
 * Experimento: E017
 *
 * Hipotesis: 3-opt puede resolver estructuras que 2-opt+or-opt no pueden (como
 * double bridges). Aplicado al mejor tour del multi-start selectivo (SolverE3),
 * puede cerrar el gap restante hacia el optimo.
 *
 * Algoritmo:
 * 1. Multi-start selectivo (vertices hull):
 *    Para cada vertice del hull:
 *      a. NN + 2-opt + or-opt + 2-opt — O(n^3)
 * 2. Sobre el mejor tour, aplicar 3-opt hasta convergencia — O(n^3)
 * 3. 2-opt final por si 3-opt abrio mejoras — O(n^3)
 *
 * Complejidad e2e: O(h * n^3 + n^3) = O(h * n^3) ≈ O(n^3.5)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.015s
 *   berlin52: ratio=1.000, tiempo=0.009s
 *   st70:     ratio=1.016, tiempo=0.020s
 *   eil76:    ratio=1.034, tiempo=0.025s
 *   rat99:    ratio=1.016, tiempo=0.049s
 *   kro200:   ratio=1.017, tiempo=0.263s
 *   a280:     ratio=1.029, tiempo=0.754s
 *   pcb442:   ratio=PENDIENTE (test interrumpido)
 *
 * Metricas agregadas (sin pcb442): Media aritmetica=1.017x | Media geometrica=1.017x | Peor caso=1.034x
 *
 * Conclusion: 3-opt sobre SolverE3 no mejora en la mayoria de instancias. El 2-opt+or-opt ya
 * elimina las estructuras que 3-opt podria corregir. Solo mejora marginalmente en kro200
 * (1.017x vs 1.023x). No justifica el coste adicional O(n^3) del 3-opt.
 */
class SolverE6 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Multi-start selectivo (como SolverE3)
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
            val afterTwoOpt = twoOpt(nnTour)
            val afterOrOpt = orOpt(afterTwoOpt)
            val afterTwoOpt2 = twoOpt(afterOrOpt)
            val tour = Tour(points = afterTwoOpt2)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 2-3: 3-opt + 2-opt final sobre el mejor tour
        val afterThreeOpt = threeOpt(bestTour!!.points)
        val finalTour = twoOpt(afterThreeOpt)

        return Tour(points = finalTour)
    }

    /** Complejidad: O(n^2) */
    private fun nearestNeighborFrom(
        points: Set<Point>,
        start: Point,
    ): List<Point> {
        val remaining = points.toMutableSet()
        val tour = mutableListOf<Point>()
        tour.add(start)
        remaining.remove(start)

        while (remaining.isNotEmpty()) {
            val current = tour.last()
            val nearest = remaining.minBy { it.distance(current) }
            tour.add(nearest)
            remaining.remove(nearest)
        }

        tour.add(tour.first())
        return tour
    }
}
