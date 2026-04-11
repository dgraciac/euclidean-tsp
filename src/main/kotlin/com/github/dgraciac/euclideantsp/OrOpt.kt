package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Aplica busqueda local or-opt sobre un tour.
 * Intenta mejorar el tour reubicando segmentos de 1, 2 o 3 puntos consecutivos
 * en una mejor posicion del tour.
 *
 * Estrategia: para cada segmento, calcula el ahorro de extraerlo y el coste
 * de insertarlo en cada posicion alternativa. Si alguna mejora el tour, la ejecuta.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^3)
 * - Por pasada: O(maxSegmentSize * n^2) = O(n^2) con maxSegmentSize constante
 * - Numero de pasadas: limitado a n (E026: empiricamente converge en ~0.2*n pasadas)
 * - Total: O(n^2) * O(n) = O(n^3)
 */
fun orOpt(tourPoints: List<Point>): List<Point> = orOpt(tourPoints, maxSegmentSize = 3)

/**
 * Version parametrizable de or-opt que permite especificar el tamaño maximo de segmento.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param maxSegmentSize tamaño maximo de segmento a reubicar (por defecto 3)
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^3) con maxSegmentSize constante
 * - Por pasada: O(maxSegmentSize * n^2) = O(n^2)
 * - Numero de pasadas: limitado a n (E026: empiricamente ~0.2*n)
 */
fun orOpt(
    tourPoints: List<Point>,
    maxSegmentSize: Int,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    var improved = true
    var maxIterations = points.size // E026: converge en ~0.2*n pasadas. Limite conservador: n.

    while (improved && maxIterations-- > 0) {
        improved = false
        for (segSize in 1..minOf(maxSegmentSize, points.size - 3)) {
            if (improved) break
            for (i in 0 until points.size - segSize) {
                if (tryRelocate(points, i, segSize)) {
                    improved = true
                    break
                }
            }
        }
    }

    return points + points.first()
}

/**
 * Intenta reubicar un segmento [i..i+segSize-1] a una mejor posicion.
 *
 * @return true si se hizo una mejora
 * Complejidad: O(n)
 */
private fun tryRelocate(
    points: MutableList<Point>,
    i: Int,
    segSize: Int,
): Boolean {
    val n = points.size
    val segEnd = i + segSize - 1

    val segFirst = points[i]
    val segLast = points[segEnd]
    val prevIdx = (i - 1 + n) % n
    val nextIdx = (i + segSize) % n

    // Ahorro de extraer el segmento
    val extractSaving =
        points[prevIdx].distance(segFirst) +
            segLast.distance(points[nextIdx]) -
            points[prevIdx].distance(points[nextIdx])

    // Indices que son parte del segmento o adyacentes (no vale insertar ahi)
    val forbidden = mutableSetOf<Int>()
    forbidden.add(prevIdx)
    for (k in 0 until segSize) {
        forbidden.add((i + k) % n)
    }

    var bestGain = 1e-10
    var bestJ = -1

    for (j in 0 until n) {
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

    if (bestJ == -1) return false

    // Extraer segmento
    val segment = (0 until segSize).map { points[i + it] }
    for (k in segSize - 1 downTo 0) {
        points.removeAt(i + k)
    }

    // Ajustar posicion de insercion
    val adjustedJ = if (bestJ > segEnd) bestJ - segSize else bestJ
    val insertAt = (adjustedJ + 1).coerceIn(0, points.size)

    points.addAll(insertAt, segment)
    return true
}
