package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ6 — Subgradient-optimized candidates + LK-2 + LK-5 (mejor de ambos)
 *
 * Linea de investigacion: J (busqueda local avanzada)
 * Padres: SolverJ5 + SubgradientOptimization
 * Experimento: E032
 *
 * Hipotesis: Los candidatos de subgradient optimization (Held-Karp) son superiores
 * a los de α-nearness basico porque los multiplicadores lagrangianos "empujan" el
 * 1-tree hacia una estructura similar a un tour. Esto produce candidatos mas
 * relevantes que mejoran tanto el 2-opt-nl como el LK.
 *
 * Algoritmo:
 * 1. Subgradient optimization (50 iter) para candidatos — O(n^2)
 * 2. Combinar subgradient(7) + dist(7) candidatos
 * 3. Multi-start(hull) + construcciones + 2-opt-nl — O(n^2.5)
 * 4. Or-opt + 2-opt — O(n^3)
 * 5. Rama A: LK-2 + DB + LK-2
 *    Rama B: LK-deep(5) + DB + LK-deep(5)
 * 6. Retornar el mejor de A y B
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.043s
 *   berlin52: ratio=1.000, tiempo=0.072s
 *   st70:     ratio=1.020, tiempo=0.058s
 *   eil76:    ratio=1.022, tiempo=0.072s
 *   rat99:    ratio=1.007, tiempo=0.092s
 *   kro200:   ratio=1.004, tiempo=0.546s
 *   a280:     ratio=1.014, tiempo=0.963s
 *   pcb442:   ratio=1.016, tiempo=3.376s
 *
 * Metricas agregadas: Media aritmetica=1.011x | Media geometrica=1.011x | Peor caso=1.022x
 *
 * Conclusion: Subgradient optimization no mejora consistentemente sobre alpha-nearness basico.
 * Mejor en eil76 (1.022x vs 1.025x) pero peor en a280 (1.014x vs 1.006x). Los 50 iteraciones
 * de subgradiente pueden ser insuficientes o los multiplicadores necesitan mejor tuning. El
 * overhead adicional no se justifica.
 */
class SolverJ6 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1-2: Subgradient candidates + distancia
        val subgradNl = buildSubgradientCandidates(instance.points, k = 7, iterations = 50)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((subgradNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Paso 3: Multi-start rapido
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        var baseTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

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
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        // Paso 4: Or-opt + 2-opt
        val afterOrOpt = orOpt(baseTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Paso 5: Rama A (LK-2) y Rama B (LK-5), mejor de ambos
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbation(afterLk2, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        val afterLk5 = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbation(afterLk5, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
