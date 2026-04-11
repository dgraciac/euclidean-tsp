package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverI2 — SolverI1 + double-bridge perturbation, todo en O(n^3)
 *
 * Linea de investigacion: I (optimizar dentro de O(n^3))
 * Padre: SolverI1
 * Experimento: E027
 *
 * Hipotesis: Añadir double-bridge perturbation (K constante intentos) al pipeline
 * de SolverI1 permite escapar el optimo local sin salir de O(n^3).
 * Double-bridge con K intentos + re-optimizacion O(n^3) = K * O(n^3) = O(n^3).
 *
 * Algoritmo:
 * 1-3. Igual que SolverI1: multi-start(hull) + 2-opt-nl + or-opt + 2-opt + LK — O(n^3)
 * 4. Double-bridge (20 intentos): cada uno perturba + 2-opt + or-opt — 20 * O(n^3) = O(n^3)
 * 5. LK final sobre el mejor — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.017s
 *   berlin52: ratio=1.000, tiempo=0.013s
 *   st70:     ratio=1.020, tiempo=0.047s
 *   eil76:    ratio=1.041, tiempo=0.032s
 *   rat99:    ratio=1.007, tiempo=0.047s
 *   kro200:   ratio=1.012, tiempo=0.303s
 *   a280:     ratio=1.017, tiempo=0.663s
 *   pcb442:   ratio=1.025, tiempo=1.886s
 *
 * Metricas agregadas: Media aritmetica=1.016x | Media geometrica=1.016x | Peor caso=1.041x
 *
 * Conclusion: Mejor solver O(n^3) del proyecto. Media 1.016x — mejora masiva sobre SolverC3
 * (1.039x). Iguala o supera a SolverH2 (O(n^3.5)) en varias instancias (rat99, a280, pcb442).
 * Double-bridge dentro de O(n^3) es la clave: 20 intentos constantes + re-optimizacion
 * O(n^3) = O(n^3). Solo 1.9s en pcb442.
 */
class SolverI2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)

        // Paso 1-2: Multi-start rapido (hull + construcciones diversas) con 2-opt-nl
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

        // Paso 3: Busqueda profunda (O(n^3))
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, neighborLists)

        // Paso 4: Double-bridge perturbation (20 intentos, cada uno O(n^3))
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 20)

        // Paso 5: LK final
        val finalTour = linKernighan(afterDb, neighborLists)

        return Tour(points = finalTour)
    }
}
