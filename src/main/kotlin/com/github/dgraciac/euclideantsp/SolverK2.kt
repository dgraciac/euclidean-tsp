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
 *   eil51:    ratio=1.007, tiempo=0.088s
 *   berlin52: ratio=1.000, tiempo=0.093s
 *   st70:     ratio=1.003, tiempo=0.130s
 *   eil76:    ratio=1.025, tiempo=0.139s
 *   rat99:    ratio=1.007, tiempo=0.251s
 *   kro200:   ratio=1.004, tiempo=1.197s
 *   a280:     ratio=1.006, tiempo=2.855s
 *   pcb442:   ratio=1.013, tiempo=9.494s
 *   d657:     ratio=1.037, tiempo=37.458s
 *   rat783:   ratio=1.026, tiempo=50.255s
 *   pr1002:   ratio=1.026, tiempo=115.484s
 *   d1291:    ratio=1.038, tiempo=217.081s
 *   d2103:    ratio=1.014, tiempo=469.384s
 *
 * Metricas agregadas: Media aritmetica=1.016x | Media geometrica=1.015x | Peor caso=1.038x
 *
 * Conclusion: Mejor solver O(n^3) del proyecto en TODAS las instancias simultaneamente.
 *   Toma lo mejor de J5 (instancias medianas) y K1 (instancias grandes).
 *   Nuevos records en st70 (1.003x), rat783 (1.026x), d1291 (1.038x).
 *   O(n^3) peor caso. 469s en d2103 (n=2103).
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
