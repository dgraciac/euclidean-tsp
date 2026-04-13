package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM3 — Solver agresivamente optimizado para escala industrial
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E054
 *
 * Hipotesis: El cuello de botella de M2 a escala grande es:
 * 1. Alpha-nearness: O(n^2) con constante alta (n DFS con HashMaps)
 * 2. buildNeighborLists: O(n^2 log n) sort completo por punto
 * 3. Multi-start NN: 20 × O(n^2)
 * Eliminando alpha-nearness (usar solo dist-NL con K mayor) y reduciendo
 * construcciones, el solver deberia ser significativamente mas rapido
 * manteniendo calidad aceptable.
 *
 * Cambios respecto a M2:
 * - Sin alpha-nearness (ahorra ~40% del tiempo)
 * - Solo distance-based NL con K=15 (compensa la falta de alpha con mas vecinos)
 * - Multi-start limitado a 6 hull points max
 * - Solo 2 construcciones (farthest insertion + greedy) en vez de 4
 * - Todo lo demas identico a M2 (NL-accelerated local search)
 *
 * Algoritmo:
 * 1. dist(15) candidates — O(n^2 log n)
 * 2. Multi-start(hull, max 6) + 2 construcciones + 2-opt-nl — O(n^2)
 * 3. Sobre el mejor tour:
 *    a. Rama LK-2: or-opt-nl + 2-opt-nl + LK(2) + DB-nl(20) + LK(2)
 *    b. Rama LK-5: or-opt-nl + 2-opt-nl + LK-deep(5) + DB-nl(20) + LK-deep(5)
 * 4. Retornar el mejor de las dos ramas
 *
 * Complejidad e2e: O(n^2 log n)
 * Complejidad peor caso: O(n^2 log n)
 */
class SolverM3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Solo distance-based NL con K=15 (sin alpha-nearness)
        val neighborLists = buildNeighborLists(instance.points, k = 15)

        // Multi-start limitado
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val allHullPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        val maxStartPoints = 6
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
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, neighborLists)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        // Solo 2 construcciones (las mas efectivas)
        val constructions =
            listOf(
                farthestInsertion(instance.points),
                greedyConstruction(instance.points),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, neighborLists)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        // Fase comun: or-opt-nl + 2-opt-nl
        val afterOrOpt = orOptWithNeighborLists(baseTour!!.points, neighborLists)
        val afterTwoOpt = twoOptWithNeighborLists(afterOrOpt, neighborLists)

        // Rama A: LK-2 + DB-nl + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, neighborLists)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, neighborLists, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, neighborLists))

        // Rama B: LK-5 + DB-nl + LK-5
        val afterLk5 = linKernighanDeep(afterTwoOpt, neighborLists, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, neighborLists, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, neighborLists, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
