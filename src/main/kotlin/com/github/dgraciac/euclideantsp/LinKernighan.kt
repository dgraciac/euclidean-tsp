package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Busqueda local Lin-Kernighan simplificada.
 *
 * Implementa el nucleo de LK: una busqueda guiada por el criterio de ganancia positiva
 * que encadena movimientos de profundidad creciente. A diferencia de k-opt fijo,
 * LK sigue cadenas de intercambios mientras la ganancia acumulada sea positiva.
 *
 * Esta implementacion usa profundidad 2 (equivalente a los mejores movimientos 3-opt).
 * Para cada arista del tour, intenta romperla y busca una cadena de 2 niveles que
 * mejore el tour. La ganancia se calcula sobre el tour RESULTANTE (no sobre la
 * cadena teorica), evitando el bug de la implementacion anterior.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @param neighborLists vecinos cercanos para restringir candidatos
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * K^2) por mejora, O(n) mejoras max = O(n^3 * K^2)
 * Con K constante: O(n^3)
 */
fun linKernighan(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    dm: DistanceMatrix? = null,
): List<Point> {
    var tour = tourPoints.dropLast(1).toMutableList()
    val n = tour.size
    if (n < 6) return tourPoints

    // Position map para O(1) lookup en vez de indexOf O(n)
    val pos = HashMap<Point, Int>(n * 2)

    fun rebuildPos() = tour.forEachIndexed { idx, p -> pos[p] = idx }
    rebuildPos()

    var improved = true
    var maxImprovements = maxOf(20, n) // E026: convergencia rapida como 2-opt

    while (improved && maxImprovements-- > 0) {
        improved = false

        for (idx1 in 0 until n) {
            if (improved) break
            val idx2 = (idx1 + 1) % n
            val t1 = tour[idx1]
            val t2 = tour[idx2]
            val distT1T2 = d(t1, t2, dm)

            val neighbors2 = neighborLists[t2] ?: continue

            for (t3 in neighbors2) {
                if (improved) break
                if (t3 == t1) continue
                val idx3 = pos[t3] ?: continue

                // Ganancia nivel 1: romper (t1,t2)
                val g1 = distT1T2 - d(t2, t3, dm)
                if (g1 <= 0) continue

                // Profundidad 1: 2-opt estandar
                val gain1 = try2OptGain(tour, n, idx1, idx3, dm)
                if (gain1 > 1e-10) {
                    applyReverse(tour, idx2, idx3, n)
                    rebuildPos()
                    improved = true
                    break
                }

                // Profundidad 2: extender la cadena
                val idx4 = (idx3 + 1) % n
                val t4 = tour[idx4]
                val neighbors4 = neighborLists[t4] ?: continue

                for (t5 in neighbors4) {
                    if (t5 == t1 || t5 == t2 || t5 == t3) continue
                    val idx5 = pos[t5] ?: continue

                    val newTour = buildDepth2Tour(tour, n, idx1, idx3, idx5, dm)
                    if (newTour != null) {
                        val oldLength = tourLength(tour, dm)
                        val newLength = tourLength(newTour, dm)
                        if (newLength < oldLength - 1e-10) {
                            tour = newTour.toMutableList()
                            rebuildPos()
                            improved = true
                            break
                        }
                    }
                }
            }
        }
    }

    return tour + tour.first()
}

/**
 * Calcula la ganancia de un movimiento 2-opt que invierte el segmento entre
 * idx1+1 e idx3 en el tour.
 *
 * Aristas eliminadas: (tour[idx1], tour[idx1+1]) y (tour[idx3], tour[idx3+1])
 * Aristas añadidas: (tour[idx1], tour[idx3]) y (tour[idx1+1], tour[idx3+1])
 *
 * Complejidad: O(1)
 */
private fun try2OptGain(
    tour: List<Point>,
    n: Int,
    idx1: Int,
    idx3: Int,
    dm: DistanceMatrix? = null,
): Double {
    val idx2 = (idx1 + 1) % n
    val idx4 = (idx3 + 1) % n
    val t1 = tour[idx1]
    val t2 = tour[idx2]
    val t3 = tour[idx3]
    val t4 = tour[idx4]
    return d(t1, t2, dm) + d(t3, t4, dm) - d(t1, t3, dm) - d(t2, t4, dm)
}

/**
 * Construye el tour resultante de un movimiento LK de profundidad 2.
 *
 * El movimiento involucra 3 aristas rotas y 3 aristas nuevas.
 * En vez de manipular segmentos in-place, construye el nuevo tour
 * explicitamente a partir de los segmentos.
 *
 * @return nuevo tour (sin cierre) o null si el movimiento no es valido
 * Complejidad: O(n)
 */
private fun buildDepth2Tour(
    tour: List<Point>,
    n: Int,
    idx1: Int,
    idx3: Int,
    idx5: Int,
    dm: DistanceMatrix? = null,
): List<Point>? {
    // Normalizar indices para que esten en orden
    val indices = listOf(idx1, idx3, idx5).sorted()
    val i = indices[0]
    val j = indices[1]
    val k = indices[2]

    // 3 segmentos: A = [i+1..j], B = [j+1..k], C = [k+1..i] (wrap-around)
    val segA = extractSegment(tour, i + 1, j, n)
    val segB = extractSegment(tour, j + 1, k, n)
    val segC = extractSegment(tour, k + 1, i, n)

    if (segA.isEmpty() || segB.isEmpty() || segC.isEmpty()) return null

    val revA = segA.reversed()
    val revB = segB.reversed()
    val revC = segC.reversed()
    val origLength = tourLength(tour, dm)

    // Probar todas las reconexiones 3-opt no triviales (que no son 2-opt ni identidad)
    // 2-opt moves (reverse one segment): ya cubiertos por twoOpt, se omiten
    // 3-opt moves genuinos: cambian el orden relativo de los segmentos
    val candidates =
        listOf(
            segA + revC + revB, // Tipo 1
            revA + revC + segB, // Tipo 2
            segC + revA + segB, // Tipo 3
            segC + segB + revA, // Tipo 4
            revB + segA + revC, // Tipo 5
        )

    var bestTour: List<Point>? = null
    var bestLength = origLength

    for (candidate in candidates) {
        if (candidate.size != n) continue
        val len = tourLength(candidate, dm)
        if (len < bestLength - 1e-10) {
            bestLength = len
            bestTour = candidate
        }
    }

    return bestTour
}

/**
 * Extrae un segmento circular del tour desde from hasta to (inclusive).
 * Complejidad: O(segmento)
 */
private fun extractSegment(
    tour: List<Point>,
    from: Int,
    to: Int,
    n: Int,
): List<Point> {
    val result = mutableListOf<Point>()
    var idx = from % n
    val end = to % n
    while (true) {
        result.add(tour[idx])
        if (idx == end) break
        idx = (idx + 1) % n
        if (result.size > n) break // Safety
    }
    return result
}

/**
 * Calcula la longitud de un tour (lista de puntos sin cierre, como ciclo).
 * Complejidad: O(n)
 */
private fun tourLength(
    tour: List<Point>,
    dm: DistanceMatrix? = null,
): Double {
    var length = 0.0
    for (i in tour.indices) {
        length += d(tour[i], tour[(i + 1) % tour.size], dm)
    }
    return length
}

/**
 * Aplica una inversion 2-opt al segmento entre from y to.
 * Complejidad: O(n)
 */
private fun applyReverse(
    tour: MutableList<Point>,
    from: Int,
    to: Int,
    n: Int,
) {
    var left = from
    var right = to
    var steps = 0
    while (left != right && (left - 1 + n) % n != right && steps < n) {
        val tmp = tour[left]
        tour[left] = tour[right]
        tour[right] = tmp
        left = (left + 1) % n
        right = (right - 1 + n) % n
        steps++
    }
}
