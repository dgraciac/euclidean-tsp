package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Aplica busqueda local 2-opt sobre un tour.
 * Intenta mejorar el tour intercambiando pares de aristas: para cada par de aristas
 * (i, i+1) y (j, j+1), si d(i, i+1) + d(j, j+1) > d(i, j) + d(i+1, j+1),
 * invierte el segmento entre i+1 y j.
 * Repite hasta que no se encuentre ninguna mejora o se alcance el limite de pasadas.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @return tour mejorado (cerrado: primero == ultimo)
 *
 * Complejidad peor caso: O(n^4)
 * - Por pasada: O(n^2) — examina todos los pares de aristas
 * - Numero de pasadas: limitado a n^2 (safety limit) — garantiza terminacion polinomica
 * - Total: O(n^2) * O(n^2) = O(n^4)
 *
 * Nota: sin el limite de pasadas, el numero de mejoras 2-opt en instancias euclideas
 * puede ser super-polinomico en el peor caso (Englert, Roeglin, Voecking 2014).
 * El limite n^2 garantiza complejidad polinomica a costa de posiblemente no converger
 * al optimo local completo.
 */
fun twoOpt(tourPoints: List<Point>): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    var improved = true
    var maxPasses = n * n // Limite de pasadas para garantizar O(n^4)

    while (improved && maxPasses-- > 0) {
        improved = false
        for (i in 0 until n - 1) {
            for (j in i + 2 until n) {
                if (i == 0 && j == n - 1) continue

                val a = points[i]
                val b = points[i + 1]
                val c = points[j]
                val d = points[(j + 1) % n]

                val currentDist = a.distance(b) + c.distance(d)
                val newDist = a.distance(c) + b.distance(d)

                if (newDist < currentDist) {
                    points.subList(i + 1, j + 1).reverse()
                    improved = true
                }
            }
        }
    }

    return points + points.first()
}
