package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverK2 — Mejor de SolverJ5 y SolverK1
 *
 * Linea de investigacion: K (optimizacion de rendimiento para mejor aproximacion)
 * Padres: SolverJ5, SolverK1
 * Experimento: E039
 *
 * Hipotesis: J5 gana en instancias medianas (n<500) gracias al DB pesado y LK profundo.
 * K1 gana en instancias grandes (n>700) gracias al multi-start completo (n starts).
 * Ejecutar ambos y quedarse con el mejor combina las fortalezas sin aumentar la complejidad.
 *
 * Algoritmo:
 * 1. Ejecutar SolverJ5
 * 2. Ejecutar SolverK1
 * 3. Retornar el mejor de los dos
 *
 * Complejidad e2e: O(n^3) — dos ejecuciones O(n^3) en secuencia
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   berlin52: ratio=PENDIENTE, tiempo=PENDIENTE
 *   st70:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   eil76:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   rat99:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   kro200:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   a280:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   pcb442:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   d657:     ratio=PENDIENTE, tiempo=PENDIENTE
 *   rat783:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   pr1002:   ratio=PENDIENTE, tiempo=PENDIENTE
 *   d1291:    ratio=PENDIENTE, tiempo=PENDIENTE
 *   d2103:    ratio=PENDIENTE, tiempo=PENDIENTE
 *
 * Metricas agregadas: PENDIENTE
 *
 * Conclusion: PENDIENTE
 */
class SolverK2 : Euclidean2DTSPSolver {
    private val solverJ5 = SolverJ5()
    private val solverK1 = SolverK1()

    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val tourJ5 = solverJ5.compute(instance)
        val tourK1 = solverK1.compute(instance)
        return if (tourJ5.length <= tourK1.length) tourJ5 else tourK1
    }
}
