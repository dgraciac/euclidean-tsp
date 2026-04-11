package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverG2 — Multi-start NN completo + construcciones diversas + 2-opt acelerado
 *
 * Linea de investigacion: G (diversidad de construccion)
 * Padres: SolverE7 + SolverG1
 * Experimento: E021
 *
 * Hipotesis: Combinar multi-start NN completo (como E7, con 2-opt acelerado para
 * velocidad) con las construcciones diversas de G1 (farthest insertion, greedy, etc.)
 * dara el mejor resultado posible: maxima diversidad de optimos locales.
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2)
 * 2. Multi-start NN desde TODOS los puntos:
 *    Para cada punto p: NN + 2-opt-nl + or-opt + 2-opt-nl — O(n^3) por start
 * 3. Construcciones diversas (4 heuristicas):
 *    Farthest insertion + local search — O(n^3)
 *    Convex hull insertion + local search — O(n^3)
 *    Peeling insertion + local search — O(n^3)
 *    Greedy construction + local search — O(n^3)
 * 4. Retornar el mejor de todos (n + 4 candidatos)
 *
 * Complejidad e2e: O(n^4) — dominada por n starts de NN
 * Complejidad peor caso: O(n^5) — (n + 4) starts × O(n^4) pipeline
 *
 * Resultados:
 *   eil51:    ratio=1.008, tiempo=0.155s
 *   berlin52: ratio=1.000, tiempo=0.134s
 *   st70:     ratio=1.015, tiempo=0.116s
 *   eil76:    ratio=1.021, tiempo=0.139s
 *   rat99:    ratio=1.008, tiempo=0.253s
 *   kro200:   ratio=1.006, tiempo=5.837s
 *   a280:     ratio=1.020, tiempo=16.668s
 *   pcb442:   ratio=1.021, tiempo=83.65s
 *
 * Metricas agregadas: Media aritmetica=1.012x | Media geometrica=1.012x | Peor caso=1.021x
 *
 * Conclusion: Mejor peor caso del proyecto (1.021x), igualando SolverE7 pero con a280
 * mejorado a 1.020x. Combina la diversidad de G1 (mejora en a280) con la aceleracion
 * de E7 (mejora en eil76). En pcb442 pierde vs SolverE2 (1.021x vs 1.018x) porque el
 * 2-opt acelerado no explora suficientes aristas en grids regulares.
 */
class SolverG2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)
        val constructions = mutableListOf<List<Point>>()

        // Multi-start NN desde todos los puntos
        for (startPoint in instance.points) {
            constructions.add(nearestNeighborFrom(instance.points, startPoint))
        }

        // Construcciones diversas
        constructions.add(farthestInsertion(instance.points))
        constructions.add(convexHullInsertion(instance.points))
        constructions.add(peelingInsertion(instance.points))
        constructions.add(greedyConstruction(instance.points))

        // Aplicar local search con 2-opt acelerado a cada construccion
        var bestTour: Tour? = null
        for (construction in constructions) {
            val afterTwoOpt = twoOptWithNeighborLists(construction, neighborLists)
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
