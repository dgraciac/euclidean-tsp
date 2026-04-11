package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverE4 — Multi-start completo + busqueda local iterativa
 *
 * Linea de investigacion: E (nearest neighbor)
 * Padre: SolverE2
 * Experimento: E014
 *
 * Hipotesis: Reemplazar el pipeline fijo (2-opt -> or-opt -> 2-opt) por busqueda local
 * iterativa (ciclo 2-opt/or-opt repetido hasta convergencia global) mejorara la calidad
 * sin cambiar la complejidad asintotica.
 *
 * Algoritmo:
 * Para cada punto p del conjunto:
 *   1. Nearest neighbor desde p — O(n^2)
 *   2. Busqueda local iterativa (ciclo 2-opt/or-opt repetido) — O(n^3)
 * Retornar el mejor tour.
 *
 * Complejidad e2e: O(n^4) — n ejecuciones de O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.064s
 *   berlin52: ratio=1.000, tiempo=0.046s
 *   st70:     ratio=1.011, tiempo=0.114s
 *   eil76:    ratio=1.027, tiempo=0.165s
 *   rat99:    ratio=1.016, tiempo=0.342s
 *   kro200:   ratio=1.006, tiempo=4.630s
 *   a280:     ratio=1.021, tiempo=13.648s
 *
 * Metricas agregadas: Media aritmetica=1.013x | Media geometrica=1.012x | Peor caso=1.027x
 *
 * Conclusion: Identico a SolverE2 en calidad. La busqueda local iterativa (repetir ciclo
 *   2-opt/or-opt) no aporta mejora sobre la pasada unica (2-opt -> or-opt -> 2-opt). Una sola
 *   pasada ya converge al optimo local. Ligeramente mas lento que E2 por la comprobacion extra.
 */
class SolverE4 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        var bestTour: Tour? = null

        for (startPoint in instance.points) {
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
