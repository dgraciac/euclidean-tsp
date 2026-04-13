package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverM1 — J5 sub-cubico: todas las fases aceleradas con neighbor lists
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverJ5
 * Experimento: E052
 *
 * Hipotesis: Se puede reducir la complejidad de J5 de O(n^3) a O(n^2 log n)
 * reemplazando todas las fases O(n^3) (2-opt completo, or-opt completo, y las
 * re-optimizaciones dentro de double-bridge) por versiones aceleradas con
 * neighbor lists, sin perder calidad significativa.
 *
 * Cambios respecto a J5:
 * - Fase comun: orOpt -> orOptWithNeighborLists, twoOpt -> twoOptWithNeighborLists
 * - Double-bridge: doubleBridgePerturbation -> doubleBridgePerturbationNl
 * - Todo lo demas identico: mismas construcciones, mismos candidatos, mismas ramas LK
 *
 * Algoritmo:
 * 1. alpha(7)+dist(7) candidates — O(n^2 log n)
 * 2. Multi-start(hull) + construcciones diversas + 2-opt-nl — O(n^2)
 * 3. Sobre el mejor tour:
 *    a. Rama LK-2: or-opt-nl + 2-opt-nl + LK(2) + DB-nl(20) + LK(2)
 *    b. Rama LK-5: or-opt-nl + 2-opt-nl + LK-deep(5) + DB-nl(20) + LK-deep(5)
 * 4. Retornar el mejor de las dos ramas
 *
 * Complejidad e2e: O(n^2 log n)
 * Desglose:
 * - Paso 1 (candidatos): O(n^2 log n) — dominante
 * - Paso 2 (construccion): O(n^2 log n) — greedy sort
 * - Paso 3a/3b (mejora local): O(n^2) — todas las fases usan NL
 * - Total: O(n^2 log n)
 *
 * Complejidad peor caso: O(n^2 log n)
 */
class SolverM1 : Euclidean2DTSPSolver {
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

        // Fase comun: or-opt-nl + 2-opt-nl (O(n^2) en lugar de O(n^3))
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
