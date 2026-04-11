package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ5 — Mejor de LK-2 y LK-5, con α-nearness combinados
 *
 * Linea de investigacion: J (busqueda local avanzada)
 * Padres: SolverJ3, SolverJ4
 * Experimento: E031b
 *
 * Hipotesis: J3 (LK-2) y J4 (LK-5) ganan en instancias diferentes.
 * Ejecutar ambos pipelines (LK-2 y LK-5) sobre el mismo tour base y quedarse
 * con el mejor combina las fortalezas de ambos sin aumentar el orden de complejidad.
 *
 * Algoritmo:
 * 1. α(7)+dist(7) candidates — O(n^2 log n)
 * 2. Multi-start(hull) + construcciones diversas + 2-opt-nl — O(n^2.5)
 * 3. Sobre el mejor tour:
 *    a. Rama LK-2: or-opt + 2-opt + LK(2) + DB(20) + LK(2)
 *    b. Rama LK-5: or-opt + 2-opt + LK-deep(5) + DB(20) + LK-deep(5)
 * 4. Retornar el mejor de las dos ramas
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — dos ramas O(n^3) en secuencia
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.037s
 *   berlin52: ratio=1.000, tiempo=0.054s
 *   st70:     ratio=1.020, tiempo=0.059s
 *   eil76:    ratio=1.025, tiempo=0.057s
 *   rat99:    ratio=1.007, tiempo=0.089s
 *   kro200:   ratio=1.004, tiempo=0.632s
 *   a280:     ratio=1.006, tiempo=0.785s
 *   pcb442:   ratio=1.013, tiempo=3.987s
 *
 * Metricas agregadas: Media aritmetica=1.010x | Media geometrica=1.010x | Peor caso=1.025x
 *
 * Conclusion: Mejor solver O(n^3). Combina las fortalezas de J3 (a280, pcb442) y J4 (kro200).
 * Media 1.010x, peor caso 1.025x. Ejecutar dos ramas (LK-2 y LK-5) y quedarse con la mejor
 * es simple pero efectivo.
 */
class SolverJ5 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start rapido
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

        // Fase comun: or-opt + 2-opt
        val afterOrOpt = orOpt(baseTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Rama A: LK-2 + DB + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbation(afterLk2, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-5 + DB + LK-5
        val afterLk5 = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbation(afterLk5, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
