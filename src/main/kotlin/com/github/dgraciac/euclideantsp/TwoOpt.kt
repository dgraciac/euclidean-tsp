package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Aplica busqueda local 2-opt sobre un tour.
 * Intenta mejorar el tour intercambiando pares de aristas: para cada par de aristas
 * (i, i+1) y (j, j+1), si d(i, i+1) + d(j, j+1) > d(i, j) + d(i+1, j+1),
 * invierte el segmento [i+1..j].
 * Repite hasta que no se encuentre ninguna mejora.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @return tour mejorado (cerrado: primero == ultimo)
 * Complejidad: O(n^2) por pasada, O(n) pasadas tipicas = O(n^3) empirico
 */
fun twoOpt(tourPoints: List<Point>): List<Point> {
    // Trabajamos sin el punto final duplicado
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    var improved = true

    while (improved) {
        improved = false
        for (i in 0 until n - 1) {
            for (j in i + 2 until n) {
                if (i == 0 && j == n - 1) continue // Misma arista en un ciclo

                val a = points[i]
                val b = points[i + 1]
                val c = points[j]
                val d = points[(j + 1) % n]

                val currentDist = a.distance(b) + c.distance(d)
                val newDist = a.distance(c) + b.distance(d)

                if (newDist < currentDist) {
                    // Invertir el segmento [i+1..j]
                    points.subList(i + 1, j + 1).reverse()
                    improved = true
                }
            }
        }
    }

    // Cerrar el tour
    return points + points.first()
}
