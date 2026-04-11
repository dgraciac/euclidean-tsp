package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverE5 — Multi-start NN + 2-opt + or-opt extendido (segmentos 1-5)
 *
 * Linea de investigacion: E (nearest neighbor)
 * Padre: SolverE2
 * Experimento: E016
 *
 * Hipotesis: Ampliar or-opt de segmentos 1-3 a segmentos 1-5 permite reubicar
 * subsecuencias mas largas, corrigiendo estructuras que or-opt(3) no puede resolver.
 *
 * Algoritmo:
 * Para cada punto p del conjunto:
 *   1. Nearest neighbor desde p — O(n^2)
 *   2. 2-opt hasta convergencia — O(n^3)
 *   3. Or-opt con segmentos 1-5 hasta convergencia — O(n^3)
 *   4. 2-opt final — O(n^3)
 * Retornar el mejor tour.
 *
 * Complejidad e2e: O(n^4) — n ejecuciones de O(n^3)
 * Complejidad peor caso: O(n^5) — n starts × O(n^4) pipeline
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.077s
 *   berlin52: ratio=1.000, tiempo=0.048s
 *   st70:     ratio=1.003, tiempo=0.152s
 *   eil76:    ratio=1.027, tiempo=0.165s
 *   rat99:    ratio=1.016, tiempo=0.303s
 *   kro200:   ratio=1.006, tiempo=4.126s
 *   a280:     ratio=1.021, tiempo=12.631s
 *   pcb442:   ratio=1.017, tiempo=65.2s
 *
 * Metricas agregadas: Media aritmetica=1.012x | Media geometrica=1.012x | Peor caso=1.027x
 *
 * Conclusion: Or-opt extendido (segmentos 1-5) mejora marginalmente en st70 (1.003x vs 1.011x)
 * y pcb442 (1.017x vs 1.018x). En la mayoria de instancias no hay diferencia. Los segmentos
 * de 4-5 puntos rara vez se reubican mejor. Mejora insuficiente para justificar el coste extra.
 */
class SolverE5 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        var bestTour: Tour? = null

        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOpt = twoOpt(nnTour)
            val afterOrOpt = orOpt(afterTwoOpt, maxSegmentSize = 5)
            val finalPoints = twoOpt(afterOrOpt)
            val tour = Tour(points = finalPoints)

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
