package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ1 — SolverI2 con α-nearness candidates (basados en 1-tree)
 *
 * Linea de investigacion: J (tecnicas LKH)
 * Padre: SolverI2
 * Experimento: E029
 *
 * Hipotesis: Reemplazar los K-nearest por distancia con K-nearest por α-nearness
 * (basado en 1-tree/MST) mejora la calidad de los candidatos. Las aristas con α
 * bajo son las mas probables de estar en el tour optimo. Esto guia tanto el 2-opt
 * con neighbor lists como el LK hacia movimientos mas productivos.
 *
 * Algoritmo:
 * 1. Precalcular α-nearness lists (K=10) basadas en MST — O(n^2 log n)
 * 2. Multi-start NN desde vertices hull + construcciones diversas:
 *    Cada uno con 2-opt(α-nl) — O(n^2) por start
 * 3. Sobre el mejor tour: or-opt + 2-opt + LK(α-nl) — O(n^3)
 * 4. Double-bridge(20) + LK(α-nl) — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.044s
 *   berlin52: ratio=1.000, tiempo=0.017s
 *   st70:     ratio=1.020, tiempo=0.065s
 *   eil76:    ratio=1.020, tiempo=0.044s
 *   rat99:    ratio=1.007, tiempo=0.056s
 *   kro200:   ratio=1.007, tiempo=0.311s
 *   a280:     ratio=1.047, tiempo=0.447s
 *   pcb442:   ratio=1.012, tiempo=2.076s
 *
 * Metricas agregadas: Media aritmetica=1.015x | Media geometrica=1.015x | Peor caso=1.047x
 *
 * Conclusion: α-nearness mejora en pcb442 (1.012x) pero empeora mucho en a280 (1.047x).
 * La lista de candidatos solo-α pierde vecinos importantes por distancia en algunas instancias.
 * Necesita combinarse con K-nearest.
 */
class SolverJ1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: α-nearness candidates basadas en 1-tree
        val alphaNl = buildAlphaNearnessList(instance.points, k = 10)

        // Paso 2: Multi-start rapido (hull + construcciones diversas) con 2-opt(α-nl)
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
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, alphaNl)
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
            val afterTwoOptNl = twoOptWithNeighborLists(construction, alphaNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 3: Busqueda profunda con α-nearness (O(n^3))
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, alphaNl)

        // Paso 4: Double-bridge + LK con α-nearness
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 20)
        val finalTour = linKernighan(afterDb, alphaNl)

        return Tour(points = finalTour)
    }
}
