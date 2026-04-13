package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM2 — M1 con infraestructura optimizada para escala industrial
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM1
 * Experimento: E053
 *
 * Hipotesis: Los cuellos de botella restantes en M1 son la infraestructura
 * (alpha-nearness con JGraphT, neighbor lists con sort completo, multi-start
 * sin limite de hull points). Optimizando estos tres puntos, M2 deberia
 * escalar a n=10,000+ manteniendo la calidad de M1/J5.
 *
 * Cambios respecto a M1:
 * - Alpha-nearness: buildAlphaNearnessListLean (sin JGraphT, O(n) memoria)
 * - Neighbor lists: buildNeighborListsLean (partial sort, evita sort completo)
 * - Multi-start: max 20 hull points (seleccion uniforme si hay mas)
 * - Todo lo demas identico a M1
 *
 * Algoritmo:
 * 1. alpha(7) lean + dist(7) lean candidates — O(n^2)
 * 2. Multi-start(hull, max 20) + construcciones diversas + 2-opt-nl — O(n^2)
 * 3. Sobre el mejor tour:
 *    a. Rama LK-2: or-opt-nl + 2-opt-nl + LK(2) + DB-nl(20) + LK(2)
 *    b. Rama LK-5: or-opt-nl + 2-opt-nl + LK-deep(5) + DB-nl(20) + LK-deep(5)
 * 4. Retornar el mejor de las dos ramas
 *
 * Complejidad e2e: O(n^2)
 * Complejidad peor caso: O(n^2)
 * Memoria: O(n * K) en lugar de O(n^2)
 */
class SolverM2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Candidatos lean (sin JGraphT, sin grafo completo)
        val alphaNl = buildAlphaNearnessListLean(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start con limite de hull points
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val allHullPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        // Limitar a max 20 start points (seleccion uniforme si hay mas)
        val maxStartPoints = 20
        val startPoints =
            if (allHullPoints.size <= maxStartPoints) {
                allHullPoints
            } else {
                val step = allHullPoints.size.toDouble() / maxStartPoints
                (0 until maxStartPoints).map { allHullPoints[(it * step).toInt()] }
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

        // Fase comun: or-opt-nl + 2-opt-nl (O(n^2))
        val afterOrOpt = orOptWithNeighborLists(baseTour!!.points, combinedNl)
        val afterTwoOpt = twoOptWithNeighborLists(afterOrOpt, combinedNl)

        // Rama A: LK-2 + DB-nl + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-5 + DB-nl + LK-5
        val afterLk5 = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
