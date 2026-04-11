package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ3 — Candidatos combinados α(7) + dist(7) + pipeline completo
 *
 * Linea de investigacion: J (tecnicas LKH)
 * Padre: SolverJ2
 * Experimento: E029
 *
 * Hipotesis: SolverJ2 con K=5+5 es inestable. Aumentar a K=7+7 (~14 candidatos por punto)
 * da mas estabilidad sin salir de O(n^3) (K sigue siendo constante).
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.020s
 *   berlin52: ratio=1.000, tiempo=0.016s
 *   st70:     ratio=1.020, tiempo=0.031s
 *   eil76:    ratio=1.025, tiempo=0.034s
 *   rat99:    ratio=1.007, tiempo=0.052s
 *   kro200:   ratio=1.012, tiempo=0.354s
 *   a280:     ratio=1.006, tiempo=0.652s
 *   pcb442:   ratio=1.013, tiempo=2.295s
 *
 * Metricas agregadas: Media aritmetica=1.011x | Media geometrica=1.011x | Peor caso=1.025x
 *
 * Conclusion: α(7)+dist(7) da la mejor estabilidad. a280: 1.006x (record O(n^3), supera SolverH3!).
 * pcb442: 1.013x. Mejor peor caso (1.025x) que SolverI2 (1.041x). Media 1.011x.
 * Mejor solver O(n^3) del proyecto.
 */
class SolverJ3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

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

        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, combinedNl)
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 20)
        val finalTour = linKernighan(afterDb, combinedNl)

        return Tour(points = finalTour)
    }
}
