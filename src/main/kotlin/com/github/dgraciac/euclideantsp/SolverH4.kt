package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverH4 — Multi-start completo + 2-opt-nl + or-opt + LK + 4-opt + DB + LK
 *
 * Linea de investigacion: H (busqueda profunda)
 * Padre: SolverH3
 * Experimento: E025
 *
 * Hipotesis: Añadir 4-opt directo (equivalente a LK profundidad 3) al pipeline
 * de SolverH3 encuentra movimientos double-bridge sin necesidad de perturbacion,
 * mejorando la calidad del tour antes de la fase de double-bridge.
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2 log n)
 * 2. Para cada punto p:
 *    a. NN + 2-opt-nl + or-opt + LK profundidad 2 — O(n^4) peor caso
 * 3. Sobre el mejor tour:
 *    a. 4-opt directo (12 candidatas) — O(n^2) peor caso
 *    b. Double-bridge (50 intentos) + LK — O(n^4) peor caso
 *
 * Complejidad e2e: O(n^4) tipica
 * Complejidad peor caso: O(n^4) — n starts × O(n^3) pipeline
 *
 * Resultados:
 *   eil51:    ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   berlin52: ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   st70:     ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   eil76:    ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   rat99:    ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   kro200:   ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   a280:     ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *   pcb442:   ratio=Identico a SolverH3, tiempo=Identico a SolverH3
 *
 * Metricas agregadas: Identico a SolverH3 en todas las instancias
 *
 * Conclusion: 4-opt directo NO aporta mejora sobre el pipeline LK + double-bridge de SolverH3.
 * El 4-opt sobre aristas candidatas es redundante: LK profundidad 2 + double-bridge perturbation
 * ya cubren los movimientos que 4-opt buscaria. El pipeline de SolverH3 es suficiente.
 */
class SolverH4 : Euclidean2DTSPSolver {
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

        // Paso 3: 4-opt + double-bridge + LK sobre el mejor
        val after4Opt = fourOpt(bestTour!!.points)
        val afterDb = doubleBridgePerturbation(after4Opt, maxAttempts = 50)
        val finalTour = linKernighan(afterDb, neighborLists)

        return Tour(points = finalTour)
    }
}
