package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Lin-Kernighan con profundidad variable (hasta 5) y backtracking.
 *
 * A diferencia de linKernighan (profundidad 2 sin backtracking), esta version:
 * 1. Explora cadenas de hasta 5 niveles de profundidad
 * 2. Cuando un nivel no mejora, backtrackea y prueba el siguiente candidato
 * 3. Prueba movimientos no secuenciales: en cada nivel, rompe aristas en AMBAS
 *    direcciones (forward y backward), permitiendo que la cadena cruce el tour
 * 4. En cada nivel, construye y evalua el tour resultante explicitamente
 *
 * El criterio de ganancia positiva poda la busqueda: en cada paso, la ganancia
 * acumulada debe ser positiva para continuar explorando.
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param neighborLists candidatos por punto
 * @param maxDepth profundidad maxima de la cadena LK
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n * K^maxDepth * n) por mejora, O(n) mejoras
 * Con K=10, maxDepth=5: O(n^2 * 10^5) = O(n^2 * 100000) por mejora
 * Para instancias pequeñas (<500): manejable. Para grandes: usar maxDepth=3.
 */
fun linKernighanDeep(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    maxDepth: Int = 5,
    dm: DistanceMatrix? = null,
): List<Point> {
    var tour = tourPoints.dropLast(1).toMutableList()
    val n = tour.size
    if (n < 6) return tourPoints

    val pos = HashMap<Point, Int>(n * 2)

    fun rebuildPos() = tour.forEachIndexed { idx, p -> pos[p] = idx }
    rebuildPos()

    var improved = true
    var maxImprovements = maxOf(20, n)

    while (improved && maxImprovements-- > 0) {
        improved = false

        for (idx1 in 0 until n) {
            if (improved) break
            val t1 = tour[idx1]
            val t2 = tour[(idx1 + 1) % n]

            // Intentar cadena LK empezando por arista (t1, t2)
            val result = lkChainSearch(tour, pos, n, t1, idx1, t2, neighborLists, maxDepth, dm)
            if (result != null) {
                tour = result.toMutableList()
                rebuildPos()
                improved = true
                break
            }
        }
    }

    return tour + tour.first()
}

/**
 * Busca recursivamente una cadena LK mejorable empezando por la arista (t1, t2).
 *
 * @param tour tour actual (sin cierre)
 * @param pos mapa de posiciones
 * @param n tamaño del tour
 * @param t1 punto fijo de la cadena (punto de cierre)
 * @param idx1 posicion de t1
 * @param t2 segundo punto de la primera arista rota
 * @param neighborLists candidatos
 * @param maxDepth profundidad maxima
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return nuevo tour mejorado o null
 */
private fun lkChainSearch(
    tour: List<Point>,
    pos: Map<Point, Int>,
    n: Int,
    t1: Point,
    idx1: Int,
    t2: Point,
    neighborLists: Map<Point, List<Point>>,
    maxDepth: Int,
    dm: DistanceMatrix? = null,
): List<Point>? {
    val distT1T2 = d(t1, t2, dm)

    // Recopilar la cadena de puntos cortados
    val cutPoints = mutableListOf(idx1)
    return lkRecurse(
        tour,
        pos,
        n,
        t1,
        idx1,
        t2,
        distT1T2,
        cutPoints,
        1,
        maxDepth,
        neighborLists,
        dm,
    )
}

/**
 * Paso recursivo de la busqueda LK.
 *
 * @param cumulativeGain ganancia acumulada hasta este punto (sum de broken - sum de added)
 * @param depth profundidad actual
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 */
private fun lkRecurse(
    tour: List<Point>,
    pos: Map<Point, Int>,
    n: Int,
    t1: Point,
    idx1: Int,
    currentEnd: Point,
    cumulativeGain: Double,
    cutPoints: MutableList<Int>,
    depth: Int,
    maxDepth: Int,
    neighborLists: Map<Point, List<Point>>,
    dm: DistanceMatrix? = null,
): List<Point>? {
    val neighbors = neighborLists[currentEnd] ?: return null
    var bestResult: List<Point>? = null
    var bestGain = 0.0

    for (candidate in neighbors) {
        if (candidate == t1 && depth == 1) continue // No cerrar en depth 1 (seria trivial)
        val idxCandidate = pos[candidate] ?: continue

        // Evitar puntos ya en la cadena
        if (idxCandidate in cutPoints) continue

        // Ganancia de añadir arista (currentEnd, candidate)
        val addCost = d(currentEnd, candidate, dm)
        val gain = cumulativeGain - addCost
        if (gain <= 0 && depth > 1) continue // Criterio de ganancia positiva (relajado en depth 1)

        // Probar romper la arista en AMBAS direcciones (secuencial y no secuencial)
        // Forward: romper (candidate, next(candidate))
        // Backward: romper (prev(candidate), candidate) — movimiento no secuencial
        val directions =
            listOf(
                Pair((idxCandidate + 1) % n, tour[(idxCandidate + 1) % n]),
                Pair((idxCandidate - 1 + n) % n, tour[(idxCandidate - 1 + n) % n]),
            )

        for ((idxBreak, breakPoint) in directions) {
            if (idxBreak in cutPoints) continue
            val breakGain = d(candidate, breakPoint, dm)
            val newCumulativeGain = gain + breakGain

            // Intentar cerrar: añadir arista (breakPoint, t1)
            val closeGain = newCumulativeGain - d(breakPoint, t1, dm)
            if (closeGain > bestGain + 1e-10) {
                cutPoints.add(idxCandidate)
                val newTour = buildTourFromCuts(tour, n, cutPoints, idx1)
                cutPoints.removeLast()

                if (newTour != null && newTour.size == n) {
                    val origLen = cyclicLength(tour, dm)
                    val newLen = cyclicLength(newTour, dm)
                    if (newLen < origLen - 1e-10 && origLen - newLen > bestGain) {
                        bestGain = origLen - newLen
                        bestResult = newTour
                    }
                }
            }

            // Intentar ir mas profundo (backtracking via recursion)
            if (depth < maxDepth) {
                cutPoints.add(idxCandidate)
                val deeper =
                    lkRecurse(
                        tour,
                        pos,
                        n,
                        t1,
                        idx1,
                        breakPoint,
                        newCumulativeGain,
                        cutPoints,
                        depth + 1,
                        maxDepth,
                        neighborLists,
                        dm,
                    )
                cutPoints.removeLast()

                if (deeper != null) {
                    val deepLen = cyclicLength(deeper, dm)
                    val origLen = cyclicLength(tour, dm)
                    if (deepLen < origLen - 1e-10 && origLen - deepLen > bestGain) {
                        bestGain = origLen - deepLen
                        bestResult = deeper
                    }
                }
            }
        }
    }

    return bestResult
}

/**
 * Construye un tour a partir de los puntos de corte de la cadena LK.
 * Los puntos de corte definen donde se rompen las aristas del tour.
 * Intenta varias reconexiones y devuelve la mejor valida.
 *
 * @param cutPoints indices ordenados de los puntos de corte
 * @param startIdx indice del primer punto (t1)
 * @return nuevo tour o null si no se puede construir
 * Complejidad: O(n)
 */
private fun buildTourFromCuts(
    tour: List<Point>,
    n: Int,
    cutPoints: List<Int>,
    startIdx: Int,
): List<Point>? {
    val sorted = (cutPoints + startIdx).distinct().sorted()
    val numSegments = sorted.size

    if (numSegments < 2) return null

    // Extraer segmentos
    val segments = mutableListOf<List<Point>>()
    for (i in 0 until numSegments) {
        val from = (sorted[i] + 1) % n
        val to = sorted[(i + 1) % numSegments]
        val seg = extractSegmentDeep(tour, from, to, n)
        if (seg.isEmpty()) return null
        segments.add(seg)
    }

    // Verificar que todos los puntos estan cubiertos
    val totalPoints = segments.sumOf { it.size }
    if (totalPoints != n) return null

    // Probar reconexiones: original y las principales variantes
    val origLen = cyclicLength(tour)
    var bestTour: List<Point>? = null
    var bestLen = origLen

    // Para 2 segmentos: solo 2-opt (reverse second)
    if (numSegments == 2) {
        val candidate = segments[0] + segments[1].reversed()
        if (candidate.size == n) {
            val len = cyclicLength(candidate)
            if (len < bestLen - 1e-10) {
                bestLen = len
                bestTour = candidate
            }
        }
    }

    // Para 3 segmentos: probar reconexiones 3-opt
    if (numSegments == 3) {
        val (a, b, c) = segments
        val ra = a.reversed()
        val rb = b.reversed()
        val rc = c.reversed()
        val candidates =
            listOf(
                a + rb + c,
                a + c + b,
                a + c + rb,
                a + rc + rb,
                ra + rc + b,
                ra + b + rc,
                c + ra + b,
                c + b + ra,
            )
        for (candidate in candidates) {
            if (candidate.size != n) continue
            val len = cyclicLength(candidate)
            if (len < bestLen - 1e-10) {
                bestLen = len
                bestTour = candidate
            }
        }
    }

    // Para 4 segmentos: double-bridge y variantes
    if (numSegments == 4) {
        val (a, b, c, dd) = segments
        val candidates =
            listOf(
                a + c + b + dd, // double bridge
                a + c.reversed() + b.reversed() + dd,
                a + b.reversed() + c + dd,
                a + dd + c + b, // reverse order
            )
        for (candidate in candidates) {
            if (candidate.size != n) continue
            val len = cyclicLength(candidate)
            if (len < bestLen - 1e-10) {
                bestLen = len
                bestTour = candidate
            }
        }
    }

    // Para 5+ segmentos: probar double-bridge parcial
    if (numSegments >= 5) {
        // Solo probar la reconexion mas simple: swap de dos segmentos centrales
        val mid = numSegments / 2
        val reordered = segments.take(mid) + segments.drop(mid).reversed()
        val candidate = reordered.flatten()
        if (candidate.size == n) {
            val len = cyclicLength(candidate)
            if (len < bestLen - 1e-10) {
                bestLen = len
                bestTour = candidate
            }
        }
    }

    return bestTour
}

private fun extractSegmentDeep(
    tour: List<Point>,
    from: Int,
    to: Int,
    n: Int,
): List<Point> {
    val result = mutableListOf<Point>()
    var idx = from
    while (true) {
        result.add(tour[idx])
        if (idx == to) break
        idx = (idx + 1) % n
        if (result.size > n) break
    }
    return result
}

private fun cyclicLength(
    tour: List<Point>,
    dm: DistanceMatrix? = null,
): Double {
    var length = 0.0
    for (i in tour.indices) {
        length += d(tour[i], tour[(i + 1) % tour.size], dm)
    }
    return length
}
