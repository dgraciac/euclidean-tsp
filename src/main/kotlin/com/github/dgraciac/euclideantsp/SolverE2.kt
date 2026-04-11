package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverE2 — Multi-start nearest neighbor + 2-opt + or-opt
 *
 * Linea de investigacion: E (nearest neighbor)
 * Padre: SolverE1
 * Experimento: E013
 *
 * Hipotesis: Ya que la busqueda local domina (E006-E010), ejecutar el pipeline
 * desde N puntos de inicio distintos y quedarse con el mejor tour reducira
 * la varianza y mejorara la calidad. Cada inicio genera un tour distinto tras
 * busqueda local, y el mejor de N sera significativamente mejor que uno solo.
 *
 * Algoritmo:
 * Para cada punto p del conjunto:
 *   1. Nearest neighbor empezando en p — O(n^2)
 *   2. 2-opt hasta convergencia — O(n^3)
 *   3. Or-opt hasta convergencia — O(n^3)
 *   4. 2-opt final — O(n^3)
 * Retornar el mejor tour de todos los inicios.
 *
 * Complejidad e2e: O(n^4) — n ejecuciones de O(n^3) cada una
 * (Polinomico, pero un grado mas que Christofides)
 * Complejidad peor caso: O(n^5) — n starts × O(n^4) pipeline cada uno
 *
 * Resultados:
 *   berlin52: ratio=1.000, tiempo=0.08s (vs Christofides 1.118: mucho mejor)
 *   st70:     ratio=1.011, tiempo=0.24s (vs Christofides 1.146: mucho mejor)
 *   kro200:   ratio=1.006, tiempo=4.1s  (vs Christofides 1.163: mucho mejor)
 *   a280:     ratio=1.021, tiempo=10.5s (vs Christofides 1.164: mucho mejor)
 *
 * Metricas agregadas: Media aritmetica=1.010x | Media geometrica=1.009x | Peor caso=1.021x
 *
 * Conclusion: Mejor solver del proyecto por amplio margen. Media 1.010x, peor caso 1.021x.
 * Casi optimo en todas las instancias. Confirma que multi-start + busqueda local es
 * extremadamente efectivo. Es O(n^4) pero en la practica tarda segundos (0.08-10.5s).
 * La mejora sobre SolverE1 es dramatica: multi-start reduce el ratio de 1.041x a 1.010x.
 */
class SolverE2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        var bestTour: Tour? = null

        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOpt = twoOpt(nnTour)
            val afterOrOpt = orOpt(afterTwoOpt)
            val finalPoints = twoOpt(afterOrOpt)
            val tour = Tour(points = finalPoints)

            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        return bestTour!!
    }

    /**
     * Nearest neighbor empezando desde un punto especifico.
     *
     * @param points todos los puntos de la instancia
     * @param start punto de inicio
     * @return tour cerrado (primero == ultimo)
     * Complejidad: O(n^2)
     */
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
