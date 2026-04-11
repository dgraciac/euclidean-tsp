package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * BruteForce — Busqueda exhaustiva
 *
 * Linea de investigacion: Ninguna (baseline exacto para instancias pequeñas)
 *
 * Algoritmo:
 * 1. Genera todas las permutaciones de los puntos — O(n! * n)
 * 2. Calcula la longitud de cada permutacion — O(n) por permutacion
 * 3. Retorna la permutacion de menor longitud como tour cerrado
 *
 * Complejidad e2e: O(n! * n)
 * - Paso 1: O(n! * n) — generacion de permutaciones
 * - Paso 2: O(n! * n) — calculo de longitudes
 * - Total: O(n! * n)
 *
 * Resultados: Solo viable para instancias pequeñas (n <= 10). Encuentra el tour optimo.
 */
class BruteForce : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.isNotEmpty())

        return instance.points
            .permute()
            .minByOrNull { list: List<Point> -> list.length() }
            .let { Tour(points = it!!.plus(it.first())) }
    }
}
