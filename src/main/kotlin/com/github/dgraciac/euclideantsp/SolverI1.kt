package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverI1 — Mejor solver O(n^3): multi-start(hull) con 2-opt-nl + or-opt + LK sobre el mejor
 *
 * Linea de investigacion: I (optimizar dentro de O(n^3))
 * Experimento: E027
 *
 * Hipotesis: Al usar 2-opt con neighbor lists (O(n^2) por start tras E026), podemos
 * hacer multi-start selectivo (h = sqrt(n) starts) dentro del presupuesto O(n^3).
 * Luego aplicar or-opt + LK solo sobre el mejor tour (O(n^3) una sola vez).
 * Esto da diversidad de multi-start + busqueda profunda, todo en O(n^3).
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2 log n)
 * 2. Multi-start NN desde vertices del hull (h starts):
 *    Para cada vertice: NN O(n^2) + 2-opt-nl O(n^2) = O(n^2) por start
 *    Total: O(h * n^2) = O(n^2.5)
 * 3. Sobre el mejor tour:
 *    a. Or-opt — O(n^3)
 *    b. 2-opt completo — O(n^3)
 *    c. LK profundidad 2 — O(n^3)
 *
 * Complejidad e2e: O(n^2.5 + n^3) = O(n^3)
 * Complejidad peor caso: O(n^3) — multi-start O(n^2.5) + busqueda profunda O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.020, tiempo=0.007s
 *   berlin52: ratio=1.000, tiempo=0.007s
 *   st70:     ratio=1.020, tiempo=0.024s
 *   eil76:    ratio=1.041, tiempo=0.012s
 *   rat99:    ratio=1.025, tiempo=0.013s
 *   kro200:   ratio=1.013, tiempo=0.099s
 *   a280:     ratio=1.027, tiempo=0.172s
 *   pcb442:   ratio=1.037, tiempo=0.536s
 *
 * Metricas agregadas: Media aritmetica=1.023x | Media geometrica=1.023x | Peor caso=1.041x
 *
 * Conclusion: Gran mejora sobre SolverC3 (1.023x vs 1.039x) manteniendo O(n^3). Multi-start(hull)
 * + 2-opt-nl es rapido y da buena diversidad. Or-opt + 2-opt + LK sobre el mejor cierra el gap.
 * Pero SolverI2 es mejor gracias al double-bridge.
 */
class SolverI1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)

        // Paso 2: Multi-start NN desde hull con 2-opt-nl rapido
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        var bestTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, neighborLists)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Tambien probar construcciones diversas con 2-opt-nl rapido
        val constructions =
            listOf(
                farthestInsertion(instance.points),
                convexHullInsertion(instance.points),
                peelingInsertion(instance.points),
                greedyConstruction(instance.points),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, neighborLists)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 3: Busqueda profunda sobre el mejor tour (O(n^3))
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, neighborLists)

        return Tour(points = afterLk)
    }
}
