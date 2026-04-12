package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverL1 — Multi-start completo + α-nearness + pipeline profundo completo
 *
 * Linea de investigacion: L (solver unico optimo en todas las escalas)
 * Padres: SolverJ5 (pipeline) + ablacion E040 (multi-start completo es la clave)
 * Experimento: E040
 *
 * Hipotesis: La ablacion E040 demostro que la ventaja de K1 en n>700 viene del
 * multi-start completo (n starts), no del DB ligero. SolverL1 combina lo mejor:
 * multi-start completo de K1 + pipeline profundo de J5 (DB pesado + LK doble rama).
 * Es un unico algoritmo, no una combinacion de solvers.
 *
 * Algoritmo:
 * 1. α(7)+dist(7) candidates — O(n^2 log n)
 * 2. Multi-start NN desde TODOS los n puntos + 4 construcciones:
 *    Cada uno con 2-opt-nl — O(n) starts × O(n^2) = O(n^3)
 * 3. Sobre el mejor tour: or-opt + 2-opt + LK(2) — O(n^3)
 * 4. Rama A: DB pesado(20) + LK(2)
 *    Rama B: LK-deep(5) + DB pesado(20) + LK-deep(5)
 * 5. Retornar la mejor rama
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — cada fase es O(n^3) o menor
 *
 * Resultados:
 *   eil51:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   berlin52: ratio=PENDIENTE, tiempo=PENDIENTE
 *   st70:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   eil76:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   rat99:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   kro200:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   a280:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   pcb442:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   d657:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   rat783:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   pr1002:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   d1291:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   d2103:    ratio=PENDIENTE, tiempo=PENDIENTE
 *
 * Metricas agregadas: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverL1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Fase 1: Multi-start completo (todos los n puntos)
        var bestTour: Tour? = null
        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Tambien probar construcciones diversas
        val constructions =
            listOf(
                farthestInsertion(instance.points),
                convexHullInsertion(instance.points),
                peelingInsertion(instance.points),
                greedyConstruction(instance.points),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Fase 2: Pipeline profundo completo
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Rama A: LK-2 + DB pesado + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbation(afterLk2, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5) + DB pesado + LK-deep(5)
        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgePerturbation(afterLkDeep, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
