package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverH3 — Multi-start NN completo + 2-opt-nl + or-opt + LK en cada start
 *
 * Linea de investigacion: H (busqueda profunda)
 * Padre: SolverH2 + SolverE7
 * Experimento: E024
 *
 * Hipotesis: Aplicar LK a cada inicio de NN (no solo al mejor del multi-start selectivo)
 * encuentra mas optimos locales profundos. El 2-opt acelerado (neighbor lists) mantiene
 * el coste por start razonable, y el LK añade la capacidad de escapar optimos locales.
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2 log n)
 * 2. Para cada punto p:
 *    a. NN desde p — O(n^2)
 *    b. 2-opt con neighbor lists — O(n^3) peor caso
 *    c. Or-opt — O(n^4) peor caso
 *    d. LK profundidad 2 — O(n^4) peor caso
 * 3. Sobre el mejor tour: double-bridge (50 intentos) + LK — O(n^4) peor caso
 * 4. Retornar el mejor
 *
 * Complejidad e2e: O(n^4) tipica, dominada por n starts con LK
 * Complejidad peor caso: O(n^4) — n starts × O(n^3) pipeline
 *
 * Resultados:
 *   eil51:    ratio=1.008, tiempo=0.212s
 *   berlin52: ratio=1.000, tiempo=0.215s
 *   st70:     ratio=1.003, tiempo=0.853s
 *   eil76:    ratio=1.021, tiempo=0.767s
 *   rat99:    ratio=1.007, tiempo=1.456s
 *   kro200:   ratio=1.005, tiempo=17.849s
 *   a280:     ratio=1.014, tiempo=34.14s
 *   pcb442:   ratio=1.012, tiempo=156.175s
 *
 * Metricas agregadas: Media aritmetica=1.009x | Media geometrica=1.008x | Peor caso=1.021x
 *
 * Conclusion: Mejor solver del proyecto. Media 1.008x, peor caso 1.021x. Multi-start completo
 * con LK en cada start encuentra optimos locales mas profundos. Mejora significativa en a280
 * (1.014x vs 1.020x anterior) y pcb442 (1.012x vs 1.018x). El pipeline NN + 2-opt-nl + or-opt
 * + LK por start, seguido de DB + LK sobre el mejor, es la mejor combinacion encontrada.
 * O(n^4) peor caso, 156s en pcb442.
 */
class SolverH3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)

        // Paso 2: Multi-start completo con LK en cada start
        var bestTour: Tour? = null

        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOpt = twoOptWithNeighborLists(nnTour, neighborLists)
            val afterOrOpt = orOpt(afterTwoOpt)
            val afterLk = linKernighan(afterOrOpt, neighborLists)
            val tour = Tour(points = afterLk)

            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 3: Double-bridge + LK sobre el mejor
        val afterDb = doubleBridgePerturbation(bestTour!!.points, maxAttempts = 50)
        val finalTour = linKernighan(afterDb, neighborLists)

        return Tour(points = finalTour)
    }
}
