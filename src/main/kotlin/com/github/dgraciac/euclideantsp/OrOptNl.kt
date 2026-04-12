package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Or-opt restringido a neighbor lists.
 *
 * En vez de probar TODAS las posiciones de insercion (O(n) por segmento),
 * solo prueba posiciones adyacentes a vecinos cercanos del segmento (O(K) por segmento).
 * Esto reduce la complejidad por pasada de O(n^2) a O(n*K).
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param neighborLists vecinos cercanos por punto
 * @param maxSegmentSize tamaño maximo de segmento (por defecto 3)
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * K) por el safety limit de n pasadas
 * Con K constante: O(n^2)
 */
fun orOptWithNeighborLists(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    maxSegmentSize: Int = 3,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size

    val pos = HashMap<Point, Int>(n * 2)

    fun rebuildPos() = points.forEachIndexed { idx, p -> pos[p] = idx }
    rebuildPos()

    var improved = true
    var maxIterations = n

    while (improved && maxIterations-- > 0) {
        improved = false
        for (segSize in 1..minOf(maxSegmentSize, n - 3)) {
            if (improved) break
            for (i in 0 until n - segSize) {
                val segFirst = points[i]
                val segLast = points[i + segSize - 1]
                val prevIdx = (i - 1 + n) % n
                val nextIdx = (i + segSize) % n

                // Ahorro de extraer
                val extractSaving =
                    points[prevIdx].distance(segFirst) +
                        segLast.distance(points[nextIdx]) -
                        points[prevIdx].distance(points[nextIdx])

                // Solo probar posiciones de insercion cerca de los vecinos del segmento
                val candidates = mutableSetOf<Int>()
                val neighbors = neighborLists[segFirst] ?: continue
                for (neighbor in neighbors) {
                    val nIdx = pos[neighbor] ?: continue
                    candidates.add(nIdx)
                    candidates.add((nIdx - 1 + n) % n)
                }
                // Tambien vecinos del ultimo punto del segmento
                val neighborsLast = neighborLists[segLast] ?: emptyList()
                for (neighbor in neighborsLast) {
                    val nIdx = pos[neighbor] ?: continue
                    candidates.add(nIdx)
                    candidates.add((nIdx - 1 + n) % n)
                }

                // Filtrar posiciones invalidas (dentro o adyacentes al segmento)
                val forbidden = mutableSetOf<Int>()
                forbidden.add(prevIdx)
                for (k in 0 until segSize) forbidden.add((i + k) % n)

                var bestGain = 1e-10
                var bestJ = -1

                for (j in candidates) {
                    if (j in forbidden) continue
                    val jNext = (j + 1) % n
                    if (jNext in forbidden && jNext != nextIdx) continue

                    val a = points[j]
                    val b = points[jNext]
                    val insertCost = a.distance(segFirst) + segLast.distance(b) - a.distance(b)
                    val netGain = extractSaving - insertCost

                    if (netGain > bestGain) {
                        bestGain = netGain
                        bestJ = j
                    }
                }

                if (bestJ != -1) {
                    val segEnd = i + segSize - 1
                    val segment = (0 until segSize).map { points[i + it] }
                    for (k in segSize - 1 downTo 0) points.removeAt(i + k)
                    val adjustedJ = if (bestJ > segEnd) bestJ - segSize else bestJ
                    val insertAt = (adjustedJ + 1).coerceIn(0, points.size)
                    points.addAll(insertAt, segment)
                    rebuildPos()
                    improved = true
                    break
                }
            }
        }
    }

    return points + points.first()
}
