package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
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

    /**
     * Aplica busqueda local 2-opt sobre un tour.
     * Intenta mejorar el tour intercambiando pares de aristas.
     * Repite hasta que no se encuentre ninguna mejora.
     *
     * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
     * @return tour mejorado (cerrado: primero == ultimo)
     * Complejidad: O(n^2) por pasada, O(n) pasadas tipicas = O(n^3) empirico
     */
    private fun twoOpt(tourPoints: List<Point>): List<Point> {
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
}
