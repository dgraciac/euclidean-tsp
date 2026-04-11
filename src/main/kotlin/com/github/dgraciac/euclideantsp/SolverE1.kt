package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverE1 — Nearest neighbor + 2-opt + or-opt + 2-opt
 *
 * Linea de investigacion: E (nearest neighbor como construccion)
 * Experimento: E009
 *
 * Hipotesis: Si nearest neighbor + busqueda local da resultados similares a SolverC3,
 * entonces la busqueda local (2-opt + or-opt) es lo que domina la calidad, y la
 * estrategia de construccion importa poco. Si SolverC3 >> SolverE1, el peeling
 * produce una semilla significativamente mejor.
 *
 * Algoritmo:
 * 1. Nearest neighbor: empezar por un punto arbitrario, siempre ir al mas cercano
 *    no visitado — O(n^2)
 * 2. 2-opt hasta convergencia — O(n^3)
 * 3. Or-opt hasta convergencia — O(n^3)
 * 4. 2-opt final — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^4) — una ejecucion de NN O(n^2) + pipeline O(n^4)
 *
 * Resultados:
 *   berlin52: ratio=1.053, tiempo=0.008s
 *   st70:     ratio=1.047, tiempo=0.002s
 *   kro200:   ratio=1.016, tiempo=0.009s
 *   a280:     ratio=1.050, tiempo=0.061s
 *
 * Metricas agregadas: Media aritmetica=1.041x | Media geometrica=1.041x | Peor caso=1.053x
 *
 * Conclusion: Resultado clave. SolverE1 gana en kro200 (1.016x) y a280 (1.050x), demostrando
 * que la busqueda local domina en instancias grandes. Mejor peor caso (1.053x) que SolverC3
 * (1.069x). Nearest neighbor es la construccion mas simple posible y produce resultados
 * competitivos.
 */
class SolverE1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Nearest neighbor
        val nnTour = nearestNeighbor(instance.points)

        // Pasos 2-4: 2-opt -> or-opt -> 2-opt
        val afterTwoOpt = twoOpt(nnTour)
        val afterOrOpt = orOpt(afterTwoOpt)
        val finalTour = twoOpt(afterOrOpt)

        return Tour(points = finalTour)
    }

    /**
     * Construye un tour con la heuristica de nearest neighbor.
     * Empieza en el primer punto y siempre visita el punto no visitado mas cercano.
     *
     * @param points conjunto de puntos
     * @return tour cerrado (primero == ultimo)
     * Complejidad: O(n^2)
     */
    private fun nearestNeighbor(points: Set<Point>): List<Point> {
        val remaining = points.toMutableSet()
        val tour = mutableListOf<Point>()

        val start = remaining.first()
        tour.add(start)
        remaining.remove(start)

        while (remaining.isNotEmpty()) {
            val current = tour.last()
            val nearest = remaining.minBy { it.distance(current) }
            tour.add(nearest)
            remaining.remove(nearest)
        }

        tour.add(tour.first()) // Cerrar
        return tour
    }
}
