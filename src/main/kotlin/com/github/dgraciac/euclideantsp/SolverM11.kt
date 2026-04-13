package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM11 — M5 con alpha-nearness via Delaunay MST (elimina Prim O(n^2))
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM5
 * Experimento: E063
 *
 * Hipotesis: Reemplazar Prim O(n^2) por Delaunay+Kruskal O(n log n) para el MST
 * reduce significativamente el tiempo en instancias grandes donde Prim domina.
 * Los candidatos alpha-nearness son identicos (MST euclideo = MST de Delaunay).
 *
 * Cambios respecto a M5:
 * - buildAlphaNearnessListLean -> buildAlphaNearnessDelaunay
 * - Todo lo demas identico
 *
 * Complejidad e2e: O(n^2) — dominada por DFS para maxEdgeOnPath (n nodos × O(n) DFS)
 * y por el calculo de alpha para n×(n-1) pares. Pero Prim O(n^2) eliminado.
 */
class SolverM11 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Alpha-nearness via Delaunay MST (O(n log n) para MST, O(n^2) para alpha)
        val alphaNl = buildAlphaNearnessDelaunay(instance.points, k = 7)
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

        // Fase comun: or-opt-nl + 2-opt-nl
        val afterOrOpt = orOptWithNeighborLists(baseTour!!.points, combinedNl)
        val afterTwoOpt = twoOptWithNeighborLists(afterOrOpt, combinedNl)

        // Rama A: LK-2 + DB-nl + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5, alphaNl K=7) + DB-nl + LK-deep(5, alphaNl K=7)
        val afterLk5 = linKernighanDeep(afterTwoOpt, alphaNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, alphaNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
