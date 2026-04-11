package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverH1 — Multi-start selectivo + 2-opt + or-opt + double-bridge perturbation
 *
 * Linea de investigacion: H (perturbacion para escapar optimos locales)
 * Experimento: E022
 *
 * Hipotesis: Tras alcanzar un optimo local con 2-opt+or-opt, aplicar perturbaciones
 * double-bridge deterministas (cortar el tour en 4 segmentos y reconectar) y
 * re-optimizar permite escapar del optimo local actual y encontrar uno mejor.
 * Double-bridge es el movimiento mas simple que 2-opt y or-opt no pueden deshacer.
 *
 * Algoritmo:
 * 1. Multi-start selectivo (vertices hull):
 *    Para cada vertice: NN + 2-opt + or-opt + 2-opt — O(n^3)
 * 2. Sobre el mejor tour, aplicar double-bridge perturbation (20 intentos):
 *    Cada intento: perturbar + 2-opt + or-opt + 2-opt — O(n^3) por intento
 *
 * Complejidad e2e: O(h * n^3 + 20 * n^3) = O((h + 20) * n^3) ≈ O(n^3.5)
 * Complejidad peor caso: O(n^3.5) — h starts × O(n^3) + 20 × O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.018s
 *   berlin52: ratio=1.000, tiempo=0.015s
 *   st70:     ratio=1.014, tiempo=0.095s
 *   eil76:    ratio=1.025, tiempo=0.044s
 *   rat99:    ratio=1.015, tiempo=0.075s
 *   kro200:   ratio=1.010, tiempo=0.448s
 *   a280:     ratio=1.026, tiempo=0.875s
 *   pcb442:   ratio=1.028, tiempo=2.953s
 *
 * Metricas agregadas: Media aritmetica=1.016x | Media geometrica=1.015x | Peor caso=1.028x
 *
 * Conclusion: Double-bridge mejora sobre SolverE3 en todas las instancias (escapar optimos locales
 * funciona). Peor caso 1.028x vs 1.034x de E3. Pero no alcanza SolverE2/G2 (multi-start completo).
 * El multi-start es mas efectivo que la perturbacion para diversificar la busqueda. Muy rapido:
 * <3s en pcb442.
 */
class SolverH1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Multi-start selectivo
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        var bestTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOpt = twoOpt(nnTour)
            val afterOrOpt = orOpt(afterTwoOpt)
            val afterTwoOpt2 = twoOpt(afterOrOpt)
            val tour = Tour(points = afterTwoOpt2)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Paso 2: Double-bridge perturbation
        val afterPerturbation = doubleBridgePerturbation(bestTour!!.points, maxAttempts = 20)
        return Tour(points = afterPerturbation)
    }
}
