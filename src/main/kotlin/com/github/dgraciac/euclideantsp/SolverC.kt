package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverC — Convex hull peeling (no implementado)
 *
 * Linea de investigacion: C (descomposicion en capas de convex hull)
 * Experimento: Pendiente
 *
 * Algoritmo propuesto:
 * 1. Calcular convex hull de los nodos no conectados restantes
 * 2. Repetir hasta que no queden nodos (peeling de capas concentricas)
 * 3. Conectar los convex hulls para formar un tour
 *
 * Complejidad estimada: Depende de la estrategia de conexion de capas.
 * - Peeling: O(n^2 log n) en peor caso (k capas * O(n log n) cada hull)
 * - Conexion: Depende del metodo elegido
 *
 * Resultados: Pendiente de implementacion
 */
class SolverC : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        TODO("Not yet implemented")
    }
}
