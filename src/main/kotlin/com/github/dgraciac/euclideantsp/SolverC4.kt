package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverC4 — Peeling + intercalado de capas preservando orden + 2-opt + or-opt
 *
 * Linea de investigacion: C (descomposicion en capas de convex hull)
 * Padre: SolverC3
 * Experimento: E007
 *
 * Hipotesis: En vez de insertar puntos uno a uno, intercalar capas completas
 * preservando su orden geometrico producira una mejor semilla para la busqueda
 * local. La idea es que cada capa tiene un orden natural (el del convex hull)
 * que se deberia respetar en el tour final.
 *
 * Algoritmo:
 * 1. Pelar convex hulls sucesivos — O(n^2 log n)
 * 2. Empezar con la capa exterior como tour
 * 3. Para cada capa interior k:
 *    a. Para cada punto de la capa k (en orden del hull), encontrar la arista
 *       del tour mas cercana al punto e insertar ahi — O(n) por punto
 *    b. La diferencia con SolverC1/C2/C3 es que insertamos los puntos de cada
 *       capa en el orden en que aparecen en su hull, no en orden de ratio global
 * 4. 2-opt -> or-opt -> 2-opt — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^4) — igual que SolverC3
 *
 * Resultados:
 *   berlin52: ratio=1.000, tiempo=0.010s
 *   st70:     ratio=1.031, tiempo=0.005s
 *   kro200:   ratio=1.048, tiempo=0.017s
 *   a280:     ratio=1.069, tiempo=0.085s
 *
 * Metricas agregadas: Media aritmetica=1.037x | Media geometrica=1.036x | Peor caso=1.069x
 *
 * Conclusion: Identico a SolverC3. El orden de insercion de puntos interiores NO importa
 * cuando se aplica busqueda local. La busqueda local borra cualquier diferencia en la semilla.
 */
class SolverC4 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Peeling
        val layers = peelConvexHulls(instance.points)

        // Paso 2: Tour inicial con capa exterior
        val tourPoints = layers.first().toMutableList()
        tourPoints.add(tourPoints.first())

        // Paso 3: Insertar capas interiores preservando orden del hull
        for (layerIndex in 1 until layers.size) {
            val layer = layers[layerIndex]
            // Insertar puntos de esta capa en el orden del hull, pero cada uno
            // en la mejor posicion del tour actual (criterio ratio)
            for (point in layer) {
                val bestIndex = findBestInsertionByRatio(tourPoints, point)
                tourPoints.add(bestIndex, point)
            }
        }

        // Paso 4: 2-opt -> or-opt -> 2-opt
        val afterTwoOpt = twoOpt(tourPoints)
        val afterOrOpt = orOpt(afterTwoOpt)
        val finalTour = twoOpt(afterOrOpt)

        return Tour(points = finalTour)
    }

    /** Complejidad: O(k * n log n), peor caso O(n^2 log n) */
    private fun peelConvexHulls(points: Set<Point>): List<List<Point>> {
        val layers = mutableListOf<List<Point>>()
        val remaining = points.toMutableSet()

        while (remaining.size >= 3) {
            val coordinates = remaining.map { it.toCoordinate() }.toTypedArray()
            val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
            val hullCoordinates = hull.coordinates.dropLast(1)
            val hullPoints =
                hullCoordinates.map { coord ->
                    remaining.first { it.x == coord.x && it.y == coord.y }
                }

            if (hullPoints.size < 3) {
                layers.add(remaining.toList())
                remaining.clear()
            } else {
                layers.add(hullPoints)
                remaining.removeAll(hullPoints.toSet())
            }
        }

        if (remaining.isNotEmpty()) {
            layers.add(remaining.toList())
        }

        return layers
    }

    /** Complejidad: O(n) */
    private fun findBestInsertionByRatio(
        tour: List<Point>,
        point: Point,
    ): Int {
        var bestIndex = 1
        var bestRatio = Double.POSITIVE_INFINITY

        for (i in 0 until tour.size - 1) {
            val a = tour[i]
            val b = tour[i + 1]
            val distAB = a.distance(b)
            if (distAB == 0.0) continue
            val ratio = (a.distance(point) + point.distance(b)) / distAB
            if (ratio < bestRatio) {
                bestRatio = ratio
                bestIndex = i + 1
            }
        }
        return bestIndex
    }
}
