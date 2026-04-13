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
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^3)
 * - Por pasada: O(maxSegmentSize * n^2) = O(n^2) con maxSegmentSize constante
 * - Numero de pasadas: limitado a n (E026: empiricamente converge en ~0.2*n pasadas)
 * - Total: O(n^2) * O(n) = O(n^3)
 */
fun orOpt(
    tourPoints: List<Point>,
    dm: DistanceMatrix? = null,
): List<Point> = orOpt(tourPoints, maxSegmentSize = 3, dm = dm)

/**
 * Version parametrizable de or-opt que permite especificar el tamaño maximo de segmento.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param maxSegmentSize tamaño maximo de segmento a reubicar (por defecto 3)
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^3) con maxSegmentSize constante
 * - Por pasada: O(maxSegmentSize * n^2) = O(n^2)
 * - Numero de pasadas: limitado a n (E026: empiricamente ~0.2*n)
 */
fun orOpt(
    tourPoints: List<Point>,
    maxSegmentSize: Int,
    dm: DistanceMatrix? = null,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    var improved = true
    var maxIterations = points.size // E026: converge en ~0.2*n pasadas. Limite conservador: n.

    while (improved && maxIterations-- > 0) {
        improved = false
        for (segSize in 1..minOf(maxSegmentSize, points.size - 3)) {
            if (improved) break
            for (i in 0 until points.size - segSize) {
                if (tryRelocate(points, i, segSize, dm)) {
                    improved = true
                    break
                }
            }
        }
    }

    return points + points.first()
}

/**
 * Or-opt acelerado con neighbor lists.
 * Solo considera posiciones de insercion cercanas a los puntos del segmento
 * (vecinos del primer y ultimo punto del segmento). Reduce la busqueda
 * de O(n) a O(K) posiciones por segmento.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param neighborLists mapa de vecinos cercanos
 * @param maxSegmentSize tamaño maximo de segmento a reubicar (por defecto 3)
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * K) con K = tamaño de neighbor list
 * - Por pasada: O(n * maxSegmentSize * K) = O(n * K) con maxSegmentSize constante
 * - Numero de pasadas: limitado a max(20, n)
 * - Total: O(n) * O(n * K) = O(n^2 * K)
 * - Con K constante (e.g., K=14): O(n^2)
 */
fun orOptWithNeighborLists(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    maxSegmentSize: Int = 3,
    dm: DistanceMatrix? = null,
): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    val position = HashMap<Point, Int>(n * 2)
    points.forEachIndexed { idx, p -> position[p] = idx }

    var improved = true
    var maxIterations = maxOf(20, n)

    while (improved && maxIterations-- > 0) {
        improved = false
        for (segSize in 1..minOf(maxSegmentSize, n - 3)) {
            if (improved) break
            for (i in 0 until n - segSize) {
                if (tryRelocateNl(points, i, segSize, neighborLists, position, dm)) {
                    // Reconstruir position map tras reubicacion
                    points.forEachIndexed { idx, p -> position[p] = idx }
                    improved = true
                    break
                }
            }
        }
    }

    return points + points.first()
}

/**
 * Intenta reubicar un segmento [i..i+segSize-1] a una mejor posicion,
 * limitando la busqueda a posiciones cercanas usando neighbor lists.
 *
 * @return true si se hizo una mejora
 * Complejidad: O(K) donde K = tamaño de neighbor list
 */
private fun tryRelocateNl(
    points: MutableList<Point>,
    i: Int,
    segSize: Int,
    neighborLists: Map<Point, List<Point>>,
    position: Map<Point, Int>,
    dm: DistanceMatrix? = null,
): Boolean {
    val n = points.size
    val segEnd = i + segSize - 1
    val segFirst = points[i]
    val segLast = points[segEnd]
    val prevIdx = (i - 1 + n) % n
    val nextIdx = (i + segSize) % n

    val extractSaving =
        d(points[prevIdx], segFirst, dm) +
            d(segLast, points[nextIdx], dm) -
            d(points[prevIdx], points[nextIdx], dm)

    val forbidden = mutableSetOf<Int>()
    forbidden.add(prevIdx)
    for (k in 0 until segSize) {
        forbidden.add((i + k) % n)
    }

    // Candidatos: posiciones adyacentes a vecinos del primer y ultimo punto del segmento
    val candidates = mutableSetOf<Int>()
    for (neighbor in neighborLists[segFirst] ?: emptyList()) {
        val pos = position[neighbor] ?: continue
        candidates.add(pos)
        if (pos > 0) candidates.add(pos - 1)
    }
    for (neighbor in neighborLists[segLast] ?: emptyList()) {
        val pos = position[neighbor] ?: continue
        candidates.add(pos)
        if (pos > 0) candidates.add(pos - 1)
    }

    var bestGain = 1e-10
    var bestJ = -1

    for (j in candidates) {
        if (j < 0 || j >= n) continue
        if (j in forbidden) continue
        val jNext = (j + 1) % n
        if (jNext in forbidden && jNext != nextIdx) continue

        val a = points[j]
        val b = points[jNext]
        val insertCost = d(a, segFirst, dm) + d(segLast, b, dm) - d(a, b, dm)
        val netGain = extractSaving - insertCost

        if (netGain > bestGain) {
            bestGain = netGain
            bestJ = j
        }
    }

    if (bestJ == -1) return false

    val segment = (0 until segSize).map { points[i + it] }
    for (k in segSize - 1 downTo 0) {
        points.removeAt(i + k)
    }

    val adjustedJ = if (bestJ > segEnd) bestJ - segSize else bestJ
    val insertAt = (adjustedJ + 1).coerceIn(0, points.size)

    points.addAll(insertAt, segment)
    return true
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
    dm: DistanceMatrix? = null,
): Boolean {
    val n = points.size
    val segEnd = i + segSize - 1

    val segFirst = points[i]
    val segLast = points[segEnd]
    val prevIdx = (i - 1 + n) % n
    val nextIdx = (i + segSize) % n

    // Ahorro de extraer el segmento
    val extractSaving =
        d(points[prevIdx], segFirst, dm) +
            d(segLast, points[nextIdx], dm) -
            d(points[prevIdx], points[nextIdx], dm)

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
        val insertCost = d(a, segFirst, dm) + d(segLast, b, dm) - d(a, b, dm)
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
