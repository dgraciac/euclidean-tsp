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
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * K^2) por mejora, O(n^2) mejoras max = O(n^4 * K^2)
 * Con K constante: O(n^4)
 */
fun linKernighan(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
): List<Point> {
    var tour = tourPoints.dropLast(1).toMutableList()
    val n = tour.size
    if (n < 6) return tourPoints

    var improved = true
    var maxImprovements = n * n

    while (improved && maxImprovements-- > 0) {
        improved = false

        for (idx1 in 0 until n) {
            if (improved) break
            val idx2 = (idx1 + 1) % n
            val t1 = tour[idx1]
            val t2 = tour[idx2]
            val distT1T2 = t1.distance(t2)

            val neighbors2 = neighborLists[t2] ?: continue

            for (t3 in neighbors2) {
                if (improved) break
                if (t3 == t1) continue
                val idx3 = tour.indexOf(t3)
                if (idx3 == -1) continue

                // Ganancia nivel 1: romper (t1,t2), la nueva arista seria (t1,t3) tras reversal
                val g1 = distT1T2 - t2.distance(t3)
                if (g1 <= 0) continue

                // Profundidad 1: 2-opt estandar (reverse segment idx2..idx3)
                val gain1 = try2OptGain(tour, n, idx1, idx3)
                if (gain1 > 1e-10) {
                    applyReverse(tour, idx2, idx3, n)
                    improved = true
                    break
                }

                // Profundidad 2: intentar extender la cadena
                // Despues de un hipotetico reverse [idx2..idx3], t4 queda adyacente a t2
                val idx4 = (idx3 + 1) % n
                val t4 = tour[idx4]

                val neighbors4 = neighborLists[t4] ?: continue

                for (t5 in neighbors4) {
                    if (t5 == t1 || t5 == t2 || t5 == t3) continue
                    val idx5 = tour.indexOf(t5)
                    if (idx5 == -1) continue

                    // Probar el movimiento compuesto: construir el tour resultante
                    // y calcular su longitud
                    val newTour = buildDepth2Tour(tour, n, idx1, idx3, idx5)
                    if (newTour != null) {
                        val oldLength = tourLength(tour)
                        val newLength = tourLength(newTour)
                        if (newLength < oldLength - 1e-10) {
                            tour = newTour.toMutableList()
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
): Double {
    val idx2 = (idx1 + 1) % n
    val idx4 = (idx3 + 1) % n
    val t1 = tour[idx1]
    val t2 = tour[idx2]
    val t3 = tour[idx3]
    val t4 = tour[idx4]
    return t1.distance(t2) + t3.distance(t4) - t1.distance(t3) - t2.distance(t4)
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

    // Probar las 2 reconexiones 3-opt no triviales (que no son 2-opt)
    // Tipo 1: C + reverso(A) + B
    val tour1 = segC + segA.reversed() + segB
    // Tipo 2: C + B + reverso(A)
    val tour2 = segC + segB + segA.reversed()

    val origLength = tourLength(tour)
    val len1 = tourLength(tour1)
    val len2 = tourLength(tour2)

    val bestNew =
        if (len1 < len2) {
            if (len1 < origLength - 1e-10) tour1 else null
        } else {
            if (len2 < origLength - 1e-10) tour2 else null
        }

    return bestNew
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
private fun tourLength(tour: List<Point>): Double {
    var length = 0.0
    for (i in tour.indices) {
        length += tour[i].distance(tour[(i + 1) % tour.size])
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
