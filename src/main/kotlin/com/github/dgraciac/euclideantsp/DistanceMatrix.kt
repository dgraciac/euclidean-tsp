package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import kotlin.math.sqrt

/**
 * Matriz de distancias precalculada para un conjunto de puntos.
 *
 * Elimina el overhead de crear objetos JTS y calcular sqrt repetidamente.
 * Cada lookup es O(1) con acceso directo a array.
 *
 * @param points lista ordenada de puntos (el orden define los indices)
 *
 * Complejidad de construccion: O(n^2)
 * Memoria: O(n^2) — n*n doubles (~35MB para n=2103)
 */
class DistanceMatrix(
    val points: List<Point>,
) {
    private val n = points.size
    private val matrix: DoubleArray = DoubleArray(n * n)

    /** Mapa de punto a indice para lookup O(1) */
    val index: Map<Point, Int>

    init {
        index =
            HashMap<Point, Int>(n * 2).also { map ->
                points.forEachIndexed { i, p -> map[p] = i }
            }

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val dx = points[i].x - points[j].x
                val dy = points[i].y - points[j].y
                val d = sqrt(dx * dx + dy * dy)
                matrix[i * n + j] = d
                matrix[j * n + i] = d
            }
        }
    }

    /** Distancia entre dos puntos por indice. O(1). */
    fun dist(
        i: Int,
        j: Int,
    ): Double = matrix[i * n + j]

    /** Distancia entre dos puntos por referencia. O(1) amortizado. */
    fun dist(
        a: Point,
        b: Point,
    ): Double {
        val i = index[a] ?: return a.distance(b)
        val j = index[b] ?: return a.distance(b)
        return matrix[i * n + j]
    }

    /** Longitud de un tour (cerrado: primero == ultimo). O(n). */
    fun tourLength(tour: List<Point>): Double {
        var length = 0.0
        for (i in 0 until tour.size - 1) {
            length += dist(tour[i], tour[i + 1])
        }
        return length
    }
}

/**
 * Funcion helper para calcular distancia usando DistanceMatrix si esta disponible.
 * Si dm es null, usa Point.distance() (compatibilidad con codigo existente).
 * Si dm no es null, usa lookup O(1) en la matriz precalculada.
 *
 * Uso: `d(a, b, dm)` en vez de `a.distance(b)` en todas las funciones compartidas.
 *
 * Complejidad: O(1)
 */
fun d(
    a: Point,
    b: Point,
    dm: DistanceMatrix?,
): Double = dm?.dist(a, b) ?: a.distance(b)
