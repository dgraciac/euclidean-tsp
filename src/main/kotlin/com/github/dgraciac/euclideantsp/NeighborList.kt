package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Construye listas de vecinos cercanos para cada punto.
 * Para cada punto, almacena los K puntos mas cercanos ordenados por distancia.
 *
 * @param points conjunto de puntos
 * @param k numero de vecinos a mantener
 * @return mapa de punto -> lista de K vecinos mas cercanos (ordenados)
 * Complejidad: O(n^2 log K) — para cada punto, encontrar K menores de n candidatos
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
 * cercanos del otro. Esto reduce la busqueda de O(n^2) a O(n*K) por pasada.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param neighborLists mapa de vecinos cercanos
 * @return tour mejorado (cerrado: primero == ultimo)
 * Complejidad: O(n*K) por pasada, O(n) pasadas tipicas
 */
fun twoOptWithNeighborLists(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size

    // Indice de posicion de cada punto en el tour
    val position = HashMap<Point, Int>(n * 2)
    points.forEachIndexed { idx, p -> position[p] = idx }

    var improved = true
    while (improved) {
        improved = false
        for (i in 0 until n - 1) {
            val a = points[i]
            val b = points[i + 1]
            val distAB = a.distance(b)

            // Solo probar con vecinos cercanos de a
            val neighbors = neighborLists[a] ?: continue
            for (c in neighbors) {
                val j = position[c] ?: continue
                if (j <= i + 1 || j >= n - 1) continue
                if (i == 0 && j == n - 1) continue

                val d = points[(j + 1) % n]
                val distCD = c.distance(d)

                if (distAB + distCD > a.distance(c) + b.distance(d)) {
                    points.subList(i + 1, j + 1).reverse()
                    // Actualizar posiciones
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
