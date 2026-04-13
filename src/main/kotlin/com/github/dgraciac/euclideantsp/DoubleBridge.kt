package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Perturbacion double-bridge determinista.
 *
 * Un double-bridge es un movimiento 4-opt no secuencial que corta el tour en
 * 4 segmentos (A, B, C, D) y los reconecta como A-C-B-D. Es el movimiento
 * mas simple que no puede deshacerse con ninguna secuencia de 2-opt o or-opt,
 * lo que permite escapar de optimos locales.
 *
 * Esta implementacion es determinista: prueba double-bridges en posiciones
 * centradas en las aristas mas largas del tour (las mas probables de ser sub-optimas).
 * Para cada configuracion, re-optimiza con 2-opt + or-opt y mantiene el mejor resultado.
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param maxAttempts numero maximo de double-bridges a probar
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return mejor tour encontrado tras perturbacion + re-optimizacion
 *
 * Complejidad peor caso: O(maxAttempts * n^4)
 * - Cada intento: perturbacion O(n) + 2-opt O(n^4) + or-opt O(n^4)
 * - Con maxAttempts constante: O(n^4)
 */
fun doubleBridgePerturbation(
    tourPoints: List<Point>,
    maxAttempts: Int = 50,
    dm: DistanceMatrix? = null,
): List<Point> {
    val points = tourPoints.dropLast(1)
    val n = points.size
    if (n < 8) return tourPoints

    var bestTour = tourPoints
    var bestLength = computeTourLength(bestTour, dm)

    // Encontrar las aristas mas largas como candidatas para puntos de corte
    val edgesByLength =
        (0 until n)
            .map { i -> Pair(i, d(points[i], points[(i + 1) % n], dm)) }
            .sortedByDescending { it.second }

    // Usar las top K aristas como candidatas para los 4 puntos de corte
    val candidatePositions = edgesByLength.take(minOf(12, n)).map { it.first }.sorted()

    var attempts = 0
    // Probar todas las combinaciones de 4 puntos de corte de las candidatas
    for (a in candidatePositions.indices) {
        if (attempts >= maxAttempts) break
        for (b in a + 1 until candidatePositions.size) {
            if (attempts >= maxAttempts) break
            for (c in b + 1 until candidatePositions.size) {
                if (attempts >= maxAttempts) break

                val i1 = candidatePositions[a]
                val i2 = candidatePositions[b]
                val i3 = candidatePositions[c]

                // Verificar que los segmentos tienen al menos 1 punto
                if (i2 - i1 < 1 || i3 - i2 < 1 || n - i3 < 1) continue

                // 4 segmentos: A=[0..i1], B=[i1+1..i2], C=[i2+1..i3], D=[i3+1..n-1]
                val segA = (0..i1).map { points[it] }
                val segB = (i1 + 1..i2).map { points[it] }
                val segC = (i2 + 1..i3).map { points[it] }
                val segD = (i3 + 1 until n).map { points[it] }

                // Double-bridge: A + C + B + D
                val perturbed = (segA + segC + segB + segD).toMutableList()
                perturbed.add(perturbed.first())

                // Re-optimizar
                val afterTwoOpt = twoOpt(perturbed, dm)
                val afterOrOpt = orOpt(afterTwoOpt, dm)
                val finalTour = twoOpt(afterOrOpt, dm)
                val finalLength = computeTourLength(finalTour, dm)

                if (finalLength < bestLength - 1e-10) {
                    bestTour = finalTour
                    bestLength = finalLength
                }

                attempts++
            }
        }
    }

    return bestTour
}

/**
 * Perturbacion double-bridge con re-optimizacion acelerada por neighbor lists.
 *
 * Identica estrategia de perturbacion que [doubleBridgePerturbation], pero la
 * re-optimizacion usa versiones aceleradas con neighbor lists:
 * twoOptWithNeighborLists + orOptWithNeighborLists en lugar de twoOpt + orOpt.
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param neighborLists mapa de vecinos cercanos
 * @param maxAttempts numero maximo de double-bridges a probar
 * @param dm matriz de distancias precalculada (null = usar Point.distance())
 * @return mejor tour encontrado tras perturbacion + re-optimizacion
 *
 * Complejidad peor caso: O(maxAttempts * n^2)
 * - Cada intento: perturbacion O(n) + 2-opt-nl O(n^2) + or-opt-nl O(n^2)
 * - Con maxAttempts constante: O(n^2)
 */
fun doubleBridgePerturbationNl(
    tourPoints: List<Point>,
    neighborLists: Map<Point, List<Point>>,
    maxAttempts: Int = 50,
    dm: DistanceMatrix? = null,
): List<Point> {
    val points = tourPoints.dropLast(1)
    val n = points.size
    if (n < 8) return tourPoints

    var bestTour = tourPoints
    var bestLength = computeTourLength(bestTour, dm)

    val edgesByLength =
        (0 until n)
            .map { i -> Pair(i, d(points[i], points[(i + 1) % n], dm)) }
            .sortedByDescending { it.second }

    val candidatePositions = edgesByLength.take(minOf(12, n)).map { it.first }.sorted()

    var attempts = 0
    for (a in candidatePositions.indices) {
        if (attempts >= maxAttempts) break
        for (b in a + 1 until candidatePositions.size) {
            if (attempts >= maxAttempts) break
            for (c in b + 1 until candidatePositions.size) {
                if (attempts >= maxAttempts) break

                val i1 = candidatePositions[a]
                val i2 = candidatePositions[b]
                val i3 = candidatePositions[c]

                if (i2 - i1 < 1 || i3 - i2 < 1 || n - i3 < 1) continue

                val segA = (0..i1).map { points[it] }
                val segB = (i1 + 1..i2).map { points[it] }
                val segC = (i2 + 1..i3).map { points[it] }
                val segD = (i3 + 1 until n).map { points[it] }

                val perturbed = (segA + segC + segB + segD).toMutableList()
                perturbed.add(perturbed.first())

                // Re-optimizar con versiones NL (O(n^2) en lugar de O(n^3))
                val afterTwoOpt = twoOptWithNeighborLists(perturbed, neighborLists, dm)
                val afterOrOpt = orOptWithNeighborLists(afterTwoOpt, neighborLists, dm = dm)
                val finalTour = twoOptWithNeighborLists(afterOrOpt, neighborLists, dm)
                val finalLength = computeTourLength(finalTour, dm)

                if (finalLength < bestLength - 1e-10) {
                    bestTour = finalTour
                    bestLength = finalLength
                }

                attempts++
            }
        }
    }

    return bestTour
}

/**
 * Calcula la longitud de un tour (cerrado: primero == ultimo).
 * Complejidad: O(n)
 */
private fun computeTourLength(
    tour: List<Point>,
    dm: DistanceMatrix? = null,
): Double {
    var length = 0.0
    for (i in 0 until tour.size - 1) {
        length += d(tour[i], tour[i + 1], dm)
    }
    return length
}
