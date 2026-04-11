package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverE7 — Multi-start completo + 2-opt acelerado (neighbor lists) + or-opt
 *
 * Linea de investigacion: E
 * Padre: SolverE2
 * Experimento: E020
 *
 * Hipotesis: Al acelerar 2-opt con neighbor lists (K=10 vecinos), cada start se
 * ejecuta mucho mas rapido. Esto permite multi-start completo (n starts) en menos
 * tiempo, manteniendo calidad similar.
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2)
 * 2. Para cada punto p:
 *    a. NN desde p — O(n^2)
 *    b. 2-opt acelerado con neighbor lists — O(n*K) por pasada
 *    c. Or-opt — O(n^2) por pasada
 *    d. 2-opt acelerado final — O(n*K) por pasada
 * 3. Retornar el mejor tour
 *
 * Complejidad e2e: O(n * (n^2 + n*K * pasadas + n^2 * pasadas)) ≈ O(n^4)
 * Pero con constante mucho menor que SolverE2 gracias a K << n.
 * Complejidad peor caso: O(n^5) — n starts × (2-opt-nl O(n^3) + or-opt O(n^4) + 2-opt-nl O(n^3)) = n × O(n^4)
 *
 * Resultados:
 *   eil51:    ratio=1.008, tiempo=0.056s
 *   berlin52: ratio=1.000, tiempo=0.049s
 *   st70:     ratio=1.015, tiempo=0.110s
 *   eil76:    ratio=1.021, tiempo=0.135s
 *   rat99:    ratio=1.008, tiempo=0.254s
 *   kro200:   ratio=1.006, tiempo=5.799s
 *   a280:     ratio=1.021, tiempo=15.559s
 *   pcb442:   ratio=1.021, tiempo=82.691s
 *
 * Metricas agregadas (sin pcb442): Media aritmetica=1.011x | Media geometrica=1.011x | Peor caso=1.021x
 *
 * Conclusion: 2-opt acelerado con neighbor lists (K=10) produce resultados distintos a 2-opt
 * completo. Mejora en eil76 (1.021x vs 1.027x) y rat99 (1.008x vs 1.016x). La restriccion
 * a vecinos cercanos evita movimientos sub-optimos que 2-opt completo hace. Mejor peor caso
 * (1.021x) que SolverE2 (1.027x) sin pcb442. Tiempo similar a E2.
 */
class SolverE7 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)

        var bestTour: Tour? = null

        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOpt = twoOptWithNeighborLists(nnTour, neighborLists)
            val afterOrOpt = orOpt(afterTwoOpt)
            val finalPoints = twoOptWithNeighborLists(afterOrOpt, neighborLists)
            val tour = Tour(points = finalPoints)

            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        return bestTour!!
    }
}
