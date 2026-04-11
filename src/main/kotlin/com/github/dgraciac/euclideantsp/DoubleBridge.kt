package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Perturbacion double-bridge determinista.
 *
 * Un double-bridge es un movimiento 4-opt que corta el tour en 4 segmentos
 * y los reconecta en un orden diferente. Es el movimiento mas simple que
 * no puede deshacerse con 2-opt ni or-opt, lo que permite escapar de
 * optimos locales.
 *
 * Dado un tour A-B-C-D (4 segmentos), el double-bridge produce A-C-B-D.
 *
 * Esta implementacion es determinista: prueba double-bridges en las aristas
 * mas largas del tour (las mas probables de ser sub-optimas).
 *
 * @param tourPoints tour de entrada (cerrado: primero == ultimo)
 * @param maxPerturbations numero maximo de perturbaciones a probar
 * @return mejor tour encontrado tras perturbacion + re-optimizacion
 * Complejidad: O(maxPerturbations * n^3) — cada perturbacion requiere re-optimizar
 */
fun doubleBridgePerturbation(
    tourPoints: List<Point>,
    maxPerturbations: Int = 20,
): List<Point> {
    val points = tourPoints.dropLast(1)
    val n = points.size
    if (n < 8) return tourPoints

    var bestTour = tourPoints
    var bestLength = tourLength(bestTour)

    // Encontrar las aristas mas largas como candidatas para corte
    val edgeLengths =
        (0 until n)
            .map { i ->
                Triple(i, (i + 1) % n, points[i].distance(points[(i + 1) % n]))
            }.sortedByDescending { it.third }

    val topEdges = edgeLengths.take(minOf(maxPerturbations, edgeLengths.size))

    // Para cada par de aristas largas, probar double-bridge
    var perturbationsApplied = 0
    for (e1idx in topEdges.indices) {
        if (perturbationsApplied >= maxPerturbations) break
        for (e2idx in e1idx + 1 until topEdges.size) {
            if (perturbationsApplied >= maxPerturbations) break

            val cut1 = topEdges[e1idx].first
            val cut2 = topEdges[e2idx].first

            // Necesitamos 4 puntos de corte ordenados
            val cuts = listOf(cut1, cut2).sorted()
            if (cuts[1] - cuts[0] < 2 || n - cuts[1] < 2) continue

            // Elegir 4 puntos de corte: a, b, c, d
            val a = 0
            val b = cuts[0] + 1
            val c = cuts[1] + 1

            if (b >= c || c >= n || b < 2) continue

            // Segmentos: [0..b-1], [b..c-1], [c..n-1]
            // Double-bridge: [0..b-1] + [c..n-1] + [b..c-1]
            val seg1 = (0 until b).map { points[it] }
            val seg2 = (b until c).map { points[it] }
            val seg3 = (c until n).map { points[it] }

            val perturbedTour = (seg1 + seg3 + seg2).toMutableList()
            perturbedTour.add(perturbedTour.first())

            // Re-optimizar con 2-opt + or-opt
            val afterTwoOpt = twoOpt(perturbedTour)
            val afterOrOpt = orOpt(afterTwoOpt)
            val finalTour = twoOpt(afterOrOpt)
            val finalLength = tourLength(finalTour)

            if (finalLength < bestLength - 1e-10) {
                bestTour = finalTour
                bestLength = finalLength
            }

            perturbationsApplied++
        }
    }

    return bestTour
}

/**
 * Calcula la longitud de un tour.
 * Complejidad: O(n)
 */
private fun tourLength(tour: List<Point>): Double {
    var length = 0.0
    for (i in 0 until tour.size - 1) {
        length += tour[i].distance(tour[i + 1])
    }
    return length
}
