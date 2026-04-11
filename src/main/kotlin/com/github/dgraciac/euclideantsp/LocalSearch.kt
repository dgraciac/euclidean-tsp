package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Aplica busqueda local iterativa: alterna 2-opt y or-opt hasta que
 * ninguno de los dos produce mejora (convergencia global).
 *
 * El ciclo es:
 *   1. 2-opt hasta convergencia
 *   2. Or-opt hasta convergencia
 *   3. Si or-opt mejoro el tour, volver a 1 (2-opt puede encontrar nuevas mejoras)
 *   4. Si or-opt no mejoro, terminar
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @return tour optimizado (cerrado: primero == ultimo)
 * Complejidad: O(n^3) por ciclo, tipicamente 2-4 ciclos = O(n^3) empirico
 */
fun iterativeLocalSearch(tourPoints: List<Point>): List<Point> {
    var current = tourPoints
    var currentLength =
        current.dropLast(1).zipWithNext { a, b -> a.distance(b) }.sum() +
            current.last().distance(current.first())

    while (true) {
        // 2-opt
        val afterTwoOpt = twoOpt(current)

        // Or-opt
        val afterOrOpt = orOpt(afterTwoOpt)
        val newLength =
            afterOrOpt.dropLast(1).zipWithNext { a, b -> a.distance(b) }.sum() +
                afterOrOpt.last().distance(afterOrOpt.first())

        if (newLength < currentLength - 1e-10) {
            current = afterOrOpt
            currentLength = newLength
            // Or-opt mejoro: repetir ciclo, 2-opt podria encontrar nuevas mejoras
        } else {
            // No hubo mejora: convergencia global alcanzada
            return afterTwoOpt
        }
    }
}
