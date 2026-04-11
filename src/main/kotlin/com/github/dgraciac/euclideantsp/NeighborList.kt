package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Construye listas de vecinos cercanos para cada punto.
 * Para cada punto, almacena los K puntos mas cercanos ordenados por distancia.
 *
 * @param points conjunto de puntos
 * @param k numero de vecinos a mantener
 * @return mapa de punto -> lista de K vecinos mas cercanos (ordenados)
 *
 * Complejidad peor caso: O(n^2 log n)
 * - Para cada punto (n), ordena los n-1 candidatos: O(n log n)
 * - Total: O(n * n log n) = O(n^2 log n)
 */
fun buildNeighborLists(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val kActual = minOf(k, pointList.size - 1)

    return pointList.associateWith { point ->
        pointList
            .filter { it != point }
            .sortedBy { it.distance(point) }
            .take(kActual)
    }
}

/**
 * 2-opt acelerado con neighbor lists.
 * Solo considera intercambios donde al menos un punto esta en la lista de vecinos
 * cercanos del otro. Reduce la busqueda de O(n^2) a O(n*K) por pasada.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param neighborLists mapa de vecinos cercanos
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * n * K) = O(n^3 * K)
 * - Por pasada: O(n * K) — para cada punto, prueba K vecinos
 * - Numero de pasadas: limitado a n^2 (safety limit)
 * - Total: O(n^2) * O(n * K) = O(n^3 * K)
 * - Con K constante (e.g., K=10): O(n^3)
 */
fun twoOptWithNeighborLists(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size

    val position = HashMap<Point, Int>(n * 2)
    points.forEachIndexed { idx, p -> position[p] = idx }

    var improved = true
    var maxPasses = n * n // Limite para garantizar terminacion polinomica

    while (improved && maxPasses-- > 0) {
        improved = false
        for (i in 0 until n - 1) {
            val a = points[i]
            val b = points[i + 1]
            val distAB = a.distance(b)

            val neighbors = neighborLists[a] ?: continue
            for (c in neighbors) {
                val j = position[c] ?: continue
                if (j <= i + 1 || j >= n - 1) continue
                if (i == 0 && j == n - 1) continue

                val d = points[(j + 1) % n]
                val distCD = c.distance(d)

                if (distAB + distCD > a.distance(c) + b.distance(d)) {
                    points.subList(i + 1, j + 1).reverse()
                    for (idx in i + 1..j) {
                        position[points[idx]] = idx
                    }
                    improved = true
                    break
                }
            }
            if (improved) break
        }
    }

    return points + points.first()
}
