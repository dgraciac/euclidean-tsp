package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverB1 — Convex hull + insercion por ratio + busqueda local 2-opt
 *
 * Linea de investigacion: B (inicializacion con convex hull)
 * Padre: SolverB
 * Experimento: E001
 *
 * Hipotesis: Añadir busqueda local 2-opt despues de la construccion eliminara
 * cruces de aristas y reducira el ratio de aproximacion significativamente.
 *
 * Algoritmo:
 * 1. Construir tour con SolverB (convex hull + insercion por ratio) — O(n^4)
 * 2. Aplicar 2-opt: para cada par de aristas (i,i+1) y (j,j+1),
 *    si d(pi,pi+1) + d(pj,pj+1) > d(pi,pj) + d(pi+1,pj+1),
 *    invertir el segmento [i+1..j] — O(n^2) por pasada
 * 3. Repetir hasta que no haya mejora
 *
 * Complejidad e2e: O(n^4)
 * - Paso 1: O(n^4) — construccion con SolverB (dominante)
 * - Paso 2-3: O(n^2) por pasada * O(n) pasadas tipicas = O(n^3) empirico
 * - Total: O(n^4) dominado por la construccion
 *
 * Resultados:
 *   berlin52: ratio=1.010, tiempo=0.42s (vs Christofides 1.118: mejor)
 *   st70:     ratio=1.051, tiempo=0.83s (vs Christofides 1.130: mejor)
 *   kro200:   ratio=1.064, tiempo=64.3s (vs Christofides 1.156: mejor)
 *   a280:     ratio=1.081, tiempo=140.2s (vs Christofides 1.143: mejor)
 *
 * Metricas agregadas: Media aritmetica=1.052x | Media geometrica=1.051x | Peor caso=1.081x
 *
 * Conclusion: Hipotesis confirmada. El 2-opt mejora significativamente el tour en todas
 * las instancias. Mejora mas pronunciada en instancias grandes (a280: -13.1% vs SolverB).
 * SolverB1 supera a Christofides en TODAS las instancias, siendo ambos polinomicos.
 */
class SolverB1 : Euclidean2DTSPSolver {
    private val solverB = SolverB()

    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val initialTour = solverB.compute(instance)
        val optimizedPoints = twoOpt(initialTour.points)
        return Tour(points = optimizedPoints)
    }
}
