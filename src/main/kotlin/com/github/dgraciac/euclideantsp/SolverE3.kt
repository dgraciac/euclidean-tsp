package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverE3 — Multi-start selectivo (vertices del convex hull) + busqueda local iterativa
 *
 * Linea de investigacion: E (nearest neighbor)
 * Padre: SolverE2
 * Experimento: E015
 *
 * Hipotesis: Los vertices del convex hull son buenos puntos de inicio para NN porque
 * estan en la periferia y generan tours con estructura distinta. Al usar solo h vertices
 * del hull (donde h = O(sqrt(n)) en promedio), reducimos de O(n^4) a O(n^3.5) manteniendo
 * la mayor parte de la calidad de multi-start completo.
 *
 * Algoritmo:
 * 1. Calcular convex hull — O(n log n)
 * 2. Para cada vertice del hull (h vertices):
 *    a. Nearest neighbor desde ese vertice — O(n^2)
 *    b. Busqueda local iterativa (2-opt/or-opt repetido) — O(n^3)
 * 3. Retornar el mejor tour
 *
 * Complejidad e2e: O(h * n^3) donde h = |convex hull|. En promedio h = O(sqrt(n)),
 *   asi que O(n^3.5). Peor caso O(n^4) si h = O(n).
 * Complejidad peor caso: O(h × n^4) ≈ O(n^4.5) — h starts × O(n^4) pipeline
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.022s
 *   berlin52: ratio=1.000, tiempo=0.015s
 *   st70:     ratio=1.016, tiempo=0.022s
 *   eil76:    ratio=1.034, tiempo=0.025s
 *   rat99:    ratio=1.016, tiempo=0.054s
 *   kro200:   ratio=1.023, tiempo=0.273s
 *   a280:     ratio=1.029, tiempo=0.481s
 *
 * Metricas agregadas: Media aritmetica=1.018x | Media geometrica=1.018x | Peor caso=1.034x
 *
 * Conclusion: Excelente tradeoff. Casi tan bueno como SolverE2 pero mucho mas rapido
 *   (O(n^3.5) vs O(n^4)). Pierde algo en kro200/a280 donde hay mas puntos interiores no
 *   cubiertos por el hull. Mejor solver "rapido" del proyecto.
 */
class SolverE3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Vertices del convex hull como puntos de inicio
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        // Paso 2: Multi-start desde cada vertice del hull
        var bestTour: Tour? = null

        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val optimized = iterativeLocalSearch(nnTour)
            val tour = Tour(points = optimized)

            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        return bestTour!!
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
