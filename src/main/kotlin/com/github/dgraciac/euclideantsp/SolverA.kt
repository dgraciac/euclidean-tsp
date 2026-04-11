package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.jts.arrayOfPoints
import com.github.dgraciac.euclideantsp.jts.centroid
import com.github.dgraciac.euclideantsp.jts.findBestIndexToInsertAt
import com.github.dgraciac.euclideantsp.jts.isLinearRing
import com.github.dgraciac.euclideantsp.jts.listOfPoints
import com.github.dgraciac.euclideantsp.jts.toLineString
import com.github.dgraciac.euclideantsp.jts.toLinearRing
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Point

/**
 * SolverA — Insercion desde el centroide con criterio de distancia absoluta
 *
 * Linea de investigacion: A (inicializacion basada en centroide)
 * Experimento: E000 (baseline)
 *
 * Algoritmo:
 * 1. Calcula el centroide del convex hull de los puntos — O(n log n)
 * 2. Ordena los puntos por distancia al centroide — O(n log n)
 * 3. Toma los 3 puntos mas cercanos al centroide, genera sus 6 permutaciones,
 *    y elige la que forma el triangulo de menor perimetro como tour inicial — O(1)
 * 4. Para cada punto no conectado (n-3 iteraciones):
 *    a. Para cada punto no conectado, busca la mejor posicion de insercion
 *       usando findBestIndexToInsertAt (distancia absoluta) — O(n^2) por punto
 *    b. Inserta el punto que globalmente minimiza la distancia de insercion
 *    Subtotal por iteracion: O(n * n^2) = O(n^3)
 * 5. Repite paso 4 hasta conectar todos los puntos — n iteraciones
 *
 * Complejidad e2e: O(n^4)
 * - Paso 1-3: O(n log n)
 * - Paso 4-5: O(n) iteraciones * O(n) puntos no conectados * O(n^2) findBestIndexToInsertAt = O(n^4)
 *
 * Resultados: Comentado en ComparisonTest (underperforms vs SolverB en instancias grandes)
 */
class SolverA : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val unconnectedPoints: MutableSet<Point> = instance.points.toJTSPoints().toMutableSet()

        val centroid: Point = instance.centroid()

        val unconnectedPointsSortedByDistanceToCentroid: List<Point> =
            unconnectedPoints.sortedBy { it.distance(centroid) }

        val linearRing: LinearRing =
            unconnectedPointsSortedByDistanceToCentroid
                .take(3)
                .permute()
                .minByOrNull { permutation: List<Point> ->
                    permutation.toLineString().length
                }!!
                .let { it.plus(it.first()).toLinearRing() }
                .also { linearRing: LinearRing ->
                    if (!linearRing.isClosed) throw RuntimeException("$linearRing not closed")
                }

        linearRing.listOfPoints().dropLast(1).forEach {
            unconnectedPoints
                .remove(it)
                .let { removed: Boolean -> if (!removed) throw RuntimeException("Point not removed") }
        }

        val connectedPoints: ArrayList<Point> = arrayListOf(*linearRing.arrayOfPoints())

        while (unconnectedPoints.isNotEmpty()) {
            val bestInsertion: Pair<Point, Int> = findBestInsertion(unconnectedPoints, connectedPoints)

            connectedPoints.add(bestInsertion.second, bestInsertion.first)
            ensureLinearRing(connectedPoints)

            unconnectedPoints.remove(bestInsertion.first)
        }

        return Tour(
            points =
                connectedPoints.map {
                    com.github.dgraciac.euclideantsp.shared
                        .Point(it.x, it.y)
                },
        )
    }

    private fun ensureLinearRing(connectedPoints: ArrayList<Point>) {
        if (!connectedPoints.isLinearRing()) throw RuntimeException("Connected points are not a Linear Ring")
    }

    private fun findBestInsertion(
        unconnectedPoints: MutableSet<Point>,
        connectedPoints: ArrayList<Point>,
    ): Pair<Point, Int> {
        var bestUnconnected: Point? = null
        var bestIndexToInsertAt: Int? = null
        var minimumLength: Double = Double.POSITIVE_INFINITY

        unconnectedPoints.forEach { unconnectedPoint: Point ->

            val (subBestIndexToInsertAt: Int, subMinimumLength: Double) =
                connectedPoints.findBestIndexToInsertAt(
                    unconnectedPoint,
                )

            if (subMinimumLength < minimumLength) {
                bestUnconnected = unconnectedPoint
                bestIndexToInsertAt = subBestIndexToInsertAt
                minimumLength = subMinimumLength
            }
        }

        if (bestUnconnected == null) throw RuntimeException("Best Unconnected is null")
        if (bestIndexToInsertAt == null) throw RuntimeException("Best Index is null")
        return Pair(bestUnconnected!!, bestIndexToInsertAt!!)
    }
}
