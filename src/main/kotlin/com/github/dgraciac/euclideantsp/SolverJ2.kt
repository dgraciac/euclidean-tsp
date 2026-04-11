package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ2 — SolverI2 con candidatos combinados (α-nearness + K-nearest)
 *
 * Linea de investigacion: J (tecnicas LKH)
 * Padre: SolverJ1
 * Experimento: E029
 *
 * Hipotesis: SolverJ1 pierde candidatos importantes en algunas instancias porque
 * α-nearness puede excluir vecinos cercanos por distancia que no estan cerca en
 * el MST. Combinar ambas listas (union de α-nearest y K-nearest) captura tanto
 * los candidatos estructurales (MST) como los geometricos (distancia).
 *
 * Algoritmo: Igual que SolverJ1 pero con candidatos = union(α-nearest(5), K-nearest(5))
 * Esto da ~10 candidatos por punto combinando ambas fuentes.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.018, tiempo=0.020s
 *   berlin52: ratio=1.000, tiempo=0.015s
 *   st70:     ratio=1.020, tiempo=0.027s
 *   eil76:    ratio=1.034, tiempo=0.035s
 *   rat99:    ratio=1.007, tiempo=0.053s
 *   kro200:   ratio=1.004, tiempo=0.317s
 *   a280:     ratio=1.006, tiempo=0.446s
 *   pcb442:   ratio=1.027, tiempo=2.458s
 *
 * Metricas agregadas: Media aritmetica=1.015x | Media geometrica=1.014x | Peor caso=1.034x
 *
 * Conclusion: α(5)+dist(5) combinados son inestables. Excelente en a280 (1.006x — record O(n^3))
 * y kro200 (1.004x) pero peor en eil51 y eil76. K=5+5 es insuficiente para estabilidad.
 */
class SolverJ2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Candidatos combinados: α-nearest(5) ∪ K-nearest(5)
        val alphaNl = buildAlphaNearnessList(instance.points, k = 5)
        val distNl = buildNeighborLists(instance.points, k = 5)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Paso 2: Multi-start rapido
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
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
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
            val afterTwoOptNl = twoOptWithNeighborLists(construction, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 3: Busqueda profunda
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, combinedNl)

        // Paso 4: Double-bridge + LK
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 20)
        val finalTour = linKernighan(afterDb, combinedNl)

        return Tour(points = finalTour)
    }
}
