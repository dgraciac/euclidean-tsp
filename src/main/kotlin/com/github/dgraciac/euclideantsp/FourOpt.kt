package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Busqueda local 4-opt directa sobre aristas candidatas.
 *
 * Equivalente a lo que Lin-Kernighan a profundidad 3 encontraria, pero implementado
 * como busqueda directa: selecciona 4 posiciones de corte de las aristas mas largas
 * del tour, y prueba todas las reconexiones no triviales de los 4 segmentos resultantes.
 *
 * Las reconexiones incluyen double-bridge (A+C+B+D) y variantes con segmentos invertidos.
 * A diferencia de DoubleBridge.kt, esta funcion evalua el tour resultante DIRECTAMENTE
 * (sin re-optimizacion), buscando mejoras que son movimientos 4-opt puros.
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param maxCandidates numero de aristas candidatas para puntos de corte
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^2 * C(maxCandidates, 4) * reconexiones)
 * Con maxCandidates constante: O(n^2) — n^2 mejoras max × O(1) por busqueda
 */
fun fourOpt(
    tourPoints: List<Point>,
    maxCandidates: Int = 12,
    dm: DistanceMatrix? = null,
): List<Point> {
    var tour = tourPoints.dropLast(1).toMutableList()
    val n = tour.size
    if (n < 8) return tourPoints

    var improved = true
    var maxImprovements = n * n

    while (improved && maxImprovements-- > 0) {
        improved = false

        // Encontrar aristas mas largas como candidatas
        val candidates =
            (0 until n)
                .map { i -> Pair(i, d(tour[i], tour[(i + 1) % n], dm)) }
                .sortedByDescending { it.second }
                .take(minOf(maxCandidates, n))
                .map { it.first }
                .sorted()

        // Probar todas las combinaciones de 4 puntos de corte
        for (a in candidates.indices) {
            if (improved) break
            for (b in a + 1 until candidates.size) {
                if (improved) break
                for (c in b + 1 until candidates.size) {
                    if (improved) break
                    for (dd in c + 1 until candidates.size) {
                        val i1 = candidates[a]
                        val i2 = candidates[b]
                        val i3 = candidates[c]
                        val i4 = candidates[dd]

                        // Verificar que los 4 segmentos tienen al menos 1 punto
                        if (i2 - i1 < 1 || i3 - i2 < 1 || i4 - i3 < 1) continue
                        if (n - i4 + i1 < 1) continue

                        val result = tryFourOptMove(tour, n, i1, i2, i3, i4, dm)
                        if (result != null) {
                            tour = result.toMutableList()
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
 * Prueba todas las reconexiones no triviales de 4 segmentos.
 * Los 4 cortes estan en posiciones i1, i2, i3, i4.
 * Segmentos: A = (i1+1..i2), B = (i2+1..i3), C = (i3+1..i4), D = (i4+1..i1 wrap)
 *
 * @return nuevo tour si alguna reconexion mejora, null si ninguna mejora
 * Complejidad: O(n) — construir y evaluar cada reconexion
 */
private fun tryFourOptMove(
    tour: List<Point>,
    n: Int,
    i1: Int,
    i2: Int,
    i3: Int,
    i4: Int,
    dm: DistanceMatrix? = null,
): List<Point>? {
    val segA = (i1 + 1..i2).map { tour[it] }
    val segB = (i2 + 1..i3).map { tour[it] }
    val segC = (i3 + 1..i4).map { tour[it] }
    val segD =
        buildList {
            var idx = (i4 + 1) % n
            while (idx != (i1 + 1) % n) {
                add(tour[idx])
                idx = (idx + 1) % n
            }
        }

    if (segA.isEmpty() || segB.isEmpty() || segC.isEmpty() || segD.isEmpty()) return null

    val origLength = fourOptTourLength(tour, n, dm)
    var bestTour: List<Point>? = null
    var bestLength = origLength

    // Double-bridge: D + B + A + C
    tryReconnection(segD + segB + segA + segC, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // D + C + B + A (reverse all order)
    tryReconnection(segD + segC + segB + segA, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // D + A + C + B
    tryReconnection(segD + segA + segC + segB, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // Con inversiones: D + rev(B) + A + C
    tryReconnection(segD + segB.reversed() + segA + segC, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // D + B + rev(A) + C
    tryReconnection(segD + segB + segA.reversed() + segC, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // D + B + A + rev(C)
    tryReconnection(segD + segB + segA + segC.reversed(), bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    // D + rev(C) + rev(B) + A (double bridge con inversiones)
    tryReconnection(segD + segC.reversed() + segB.reversed() + segA, bestLength, dm)?.let {
        bestLength = fourOptTourLength(it, it.size, dm)
        bestTour = it
    }

    return bestTour
}

private fun tryReconnection(
    candidate: List<Point>,
    currentBest: Double,
    dm: DistanceMatrix? = null,
): List<Point>? {
    val len = fourOptTourLength(candidate, candidate.size, dm)
    return if (len < currentBest - 1e-10) candidate else null
}

private fun fourOptTourLength(
    tour: List<Point>,
    n: Int,
    dm: DistanceMatrix? = null,
): Double {
    var length = 0.0
    for (i in 0 until n) {
        length += d(tour[i], tour[(i + 1) % n], dm)
    }
    return length
}
