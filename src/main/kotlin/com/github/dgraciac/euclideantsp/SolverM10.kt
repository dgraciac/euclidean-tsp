package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM10 — M5 con alpha-nearness optimizada (binary lifting + KD-tree candidatos)
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM5
 * Experimento: E062
 *
 * Hipotesis: Reemplazar buildAlphaNearnessListLean (n DFS de O(n) = O(n^2) para maxEdgeOnPath)
 * por buildAlphaNearnessHld (binary lifting O(log n) por consulta, solo K'=4K candidatos por nodo)
 * reduce el coste de alpha-nearness. Combinado con KD-tree para la preseleccion de candidatos.
 *
 * Nota: Prim MST sigue siendo O(n^2) — inevitable sin estructura espacial para MST en plano.
 * La mejora esta en el paso 2 (maxEdgeOnPath) y paso 3 (solo K' candidatos, no n-1).
 *
 * Complejidad e2e: O(n^2) — dominada por Prim. Pero el factor constante deberia ser menor
 * que M5 porque evita n DFS completos.
 */
class SolverM10 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Alpha-nearness con binary lifting (O(n^2) Prim + O(n K' log n) consultas)
        val alphaNl = buildAlphaNearnessHld(instance.points, k = 7)
        val distNl = buildNeighborListsKdTree(instance.points, k = 7)
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
