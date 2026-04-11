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
 * Complejidad peor caso: O(n^3)
 * - Por pasada: O(n^2) — examina todos los pares de aristas
 * - Numero de pasadas: limitado a max(20, n) — E026 demostro que empiricamente converge
 *   en <=6 pasadas (constante) en todas las instancias probadas (n=51 a n=442).
 *   Limite conservador max(20, n) garantiza O(n) pasadas y terminacion polinomica.
 * - Total: O(n^2) * O(n) = O(n^3)
 *
 * Nota historica: el safety limit anterior era n^2 (O(n^4) peor caso). E026 demostro
 * que es excesivo — 2-opt nunca necesito mas de 6 pasadas en 80 ejecuciones sobre
 * 8 instancias TSPLIB.
 */
fun twoOpt(tourPoints: List<Point>): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    var improved = true
    var maxPasses = maxOf(20, n) // E026: empiricamente converge en <=6 pasadas. Limite conservador.

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
