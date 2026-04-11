package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverC3 — Convex hull peeling + insercion + 2-opt + or-opt
 *
 * Linea de investigacion: C (descomposicion en capas de convex hull)
 * Padre: SolverC2
 * Experimento: E005
 *
 * Hipotesis: Añadir or-opt despues de 2-opt reubicara segmentos mal posicionados
 * que 2-opt no puede corregir (ya que 2-opt solo invierte segmentos, no los reubica).
 * Mejora esperada: 1-3% adicional sobre SolverC2.
 *
 * Algoritmo:
 * 1. Pelar convex hulls sucesivos — O(n^2 log n)
 * 2. Insertar capas interiores por ratio — O(n^2)
 * 3. Aplicar 2-opt hasta convergencia — O(n^3)
 * 4. Aplicar or-opt (segmentos de 1, 2, 3 puntos) hasta convergencia — O(n^3)
 * 5. Repetir 2-opt por si or-opt abrio nuevas mejoras — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * - Pasos 1-2: O(n^2 log n)
 * - Pasos 3-5: O(n^3) — busqueda local (dominante)
 * - Total: O(n^3)
 *
 * Resultados:
 *   berlin52: ratio=1.000, tiempo=0.008s (vs Christofides 1.126: mucho mejor, casi optimo!)
 *   st70:     ratio=1.031, tiempo=0.006s (vs Christofides 1.128: mejor)
 *   kro200:   ratio=1.048, tiempo=0.059s (vs Christofides 1.154: mejor)
 *   a280:     ratio=1.069, tiempo=0.036s (vs Christofides 1.143: mejor)
 *
 * Metricas agregadas: Media aritmetica=1.037x | Media geometrica=1.036x | Peor caso=1.069x
 *
 * Conclusion: Excelente resultado. SolverC3 es el mejor solver del proyecto en todas las metricas.
 * Supera a todos los solvers previos incluido SolverB1 (O(n^4)). En berlin52 alcanza ratio 1.0003,
 * practicamente el tour optimo. El or-opt mejora significativamente sobre el 2-opt puro.
 * Es O(n^3), mismo coste que Christofides, pero con aproximacion dramaticamente mejor.
 */
class SolverC3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Pasos 1-2: Peeling + insercion
        val layers = peelConvexHulls(instance.points)
        val tourPoints = layers.first().toMutableList()
        tourPoints.add(tourPoints.first())

        for (layerIndex in 1 until layers.size) {
            for (point in layers[layerIndex]) {
                val bestIndex = findBestInsertionByRatio(tourPoints, point)
                tourPoints.add(bestIndex, point)
            }
        }

        // Pasos 3-5: 2-opt -> or-opt -> 2-opt
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

            val insertionDist = a.distance(point) + point.distance(b)
            val ratio = insertionDist / distAB

            if (ratio < bestRatio) {
                bestRatio = ratio
                bestIndex = i + 1
            }
        }

        return bestIndex
    }
}
