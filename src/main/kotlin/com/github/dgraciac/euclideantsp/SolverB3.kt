package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverB3 — Convex hull + insercion optimizada + 2-opt + or-opt + 2-opt
 *
 * Linea de investigacion: B (inicializacion con convex hull)
 * Padre: SolverB2
 * Experimento: E006
 *
 * Hipotesis: Al aplicar el mismo pipeline que SolverC3 (2-opt + or-opt + 2-opt) pero
 * usando convex hull simple en vez de peeling, podemos aislar el efecto de la estrategia
 * de construccion. Si SolverB3 ≈ SolverC3, la busqueda local domina. Si SolverC3 > SolverB3,
 * el peeling produce una semilla superior.
 *
 * Algoritmo:
 * 1. Calcular convex hull — O(n log n)
 * 2. Insertar puntos interiores por ratio (sin validacion) — O(n^2)
 * 3. 2-opt hasta convergencia — O(n^3)
 * 4. Or-opt hasta convergencia — O(n^3)
 * 5. 2-opt final — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 *
 * Resultados:
 *   berlin52: ratio=1.042, tiempo=0.010s
 *   st70:     ratio=1.020, tiempo=0.003s
 *   kro200:   ratio=1.041, tiempo=0.025s
 *   a280:     ratio=1.055, tiempo=0.064s
 *
 * Metricas agregadas: Media aritmetica=1.040x | Media geometrica=1.039x | Peor caso=1.055x
 *
 * Conclusion: SolverB3 es competitivo con SolverC3 — la busqueda local domina sobre la
 * construccion. Gana en st70 (1.020x vs 1.031x). Confirma que peeling vs convex hull importa
 * poco cuando se aplica 2-opt + or-opt.
 */
class SolverB3 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Convex hull como tour inicial
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)

        val remaining = instance.points.toMutableSet()
        val tourPoints =
            hullCoords
                .map { coord ->
                    remaining.first { it.x == coord.x && it.y == coord.y }
                }.toMutableList()
        tourPoints.forEach { remaining.remove(it) }
        tourPoints.add(tourPoints.first())

        // Paso 2: Insercion sin validacion
        for (point in remaining) {
            val bestIndex = findBestInsertionByRatio(tourPoints, point)
            tourPoints.add(bestIndex, point)
        }

        // Pasos 3-5: 2-opt -> or-opt -> 2-opt
        val afterTwoOpt = twoOpt(tourPoints)
        val afterOrOpt = orOpt(afterTwoOpt)
        val finalTour = twoOpt(afterOrOpt)

        return Tour(points = finalTour)
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
