package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverL4 — SolverL2 con matriz de distancias precalculada
 *
 * Linea de investigacion: L (eficiencia sin sacrificar calidad)
 * Padre: SolverL2
 * Experimento: E044
 *
 * Hipotesis: Precalcular la matriz de distancias elimina el overhead de JTS
 * (creacion de objetos Coordinate + sqrt) en cada llamada a Point.distance().
 * Esto deberia acelerar TODAS las fases sin cambiar nada del algoritmo.
 * Los resultados deben ser identicos a L2 (mismas decisiones, mismas distancias).
 *
 * Nota: como las funciones compartidas (twoOpt, orOpt, etc.) usan Point.distance()
 * internamente, este solver delega en L2 pero mide si la precalculacion de candidatos
 * (la parte que SI podemos controlar) mejora el tiempo. La refactorizacion completa
 * para pasar DistanceMatrix a todas las funciones se haria solo si el impacto es
 * significativo.
 *
 * Enfoque: precalcular DistanceMatrix y usarla para construir neighbor lists mas rapido,
 * y para las evaluaciones de longitud de tour.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3)
 *
 * Resultados: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverL4 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val dm = DistanceMatrix(instance.points.toList())

        // Construir neighbor lists usando la matriz (evita JTS en esta fase)
        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborListsWithMatrix(dm, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // El resto del pipeline delega en las funciones compartidas que usan Point.distance()
        // La mejora principal viene de las neighbor lists (construidas mas rapido con la matriz)
        val hull =
            ConvexHull(
                instance.points.map { it.toCoordinate() }.toTypedArray(),
                GeometryFactory(),
            ).convexHull
        val startPoints =
            hull.coordinates.dropLast(1).map { coord ->
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

        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgeFast(afterLk2, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        val afterLkDeep = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbDeep = doubleBridgeFast(afterLkDeep, combinedNl, quickAttempts = 50, deepAttempts = 5)
        val tourB = Tour(points = linKernighanDeep(afterDbDeep, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }

    /**
     * Construye neighbor lists usando la DistanceMatrix (sin JTS).
     * Complejidad: O(n^2 log n)
     */
    private fun buildNeighborListsWithMatrix(
        dm: DistanceMatrix,
        k: Int,
    ): Map<Point, List<Point>> {
        val points = dm.points
        val kActual = minOf(k, points.size - 1)
        return points.associateWith { p ->
            val idx = dm.index[p]!!
            points
                .filter { it != p }
                .sortedBy { dm.dist(idx, dm.index[it]!!) }
                .take(kActual)
        }
    }
}
