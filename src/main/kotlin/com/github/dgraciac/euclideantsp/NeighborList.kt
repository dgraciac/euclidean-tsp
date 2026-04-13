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
            .sortedWith(compareBy<Point> { it.distance(point) }.thenBy { it.x }.thenBy { it.y })
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
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * K)
 * - Por pasada: O(n * K) — para cada punto, prueba K vecinos
 * - Numero de pasadas: limitado a max(20, n) (E026: empiricamente <=6 pasadas)
 * - Total: O(n) * O(n * K) = O(n^2 * K)
 * - Con K constante (e.g., K=10): O(n^2)
 */
fun twoOptWithNeighborLists(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    dm: DistanceMatrix? = null,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size

    val position = HashMap<Point, Int>(n * 2)
    points.forEachIndexed { idx, p -> position[p] = idx }

    var improved = true
    var maxPasses = maxOf(20, n) // E026: empiricamente <=6 pasadas. Limite conservador.

    while (improved && maxPasses-- > 0) {
        improved = false
        for (i in 0 until n - 1) {
            val a = points[i]
            val b = points[i + 1]
            val distAB = d(a, b, dm)

            val neighbors = neighborLists[a] ?: continue
            for (c in neighbors) {
                val j = position[c] ?: continue
                if (j <= i + 1 || j >= n - 1) continue
                if (i == 0 && j == n - 1) continue

                val dd = points[(j + 1) % n]
                val distCD = d(c, dd, dm)

                if (distAB + distCD > d(a, c, dm) + d(b, dd, dm)) {
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
