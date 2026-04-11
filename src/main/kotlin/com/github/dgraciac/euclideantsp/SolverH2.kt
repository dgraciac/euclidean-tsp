package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverH2 — Multi-start selectivo + LK + double-bridge + re-LK
 *
 * Linea de investigacion: H (busqueda profunda para escapar optimos locales)
 * Padres: SolverH1 + LinKernighan
 * Experimento: E023
 *
 * Hipotesis: Combinar LK correcto (profundidad 2) con double-bridge perturbation
 * encuentra mejoras que ninguno de los dos encuentra por separado. El pipeline es:
 * multi-start selectivo → 2-opt → or-opt → LK → double-bridge → 2-opt → or-opt → LK
 *
 * Algoritmo:
 * 1. Precalcular neighbor lists (K=10) — O(n^2 log n)
 * 2. Multi-start selectivo (vertices hull):
 *    Para cada vertice: NN + 2-opt + or-opt + 2-opt — O(n^4) peor caso
 * 3. Sobre el mejor tour: LK profundidad 2 — O(n^4) peor caso
 * 4. Double-bridge perturbation (50 intentos) — O(n^4) peor caso
 * 5. LK final — O(n^4) peor caso
 *
 * Complejidad e2e: O(h * n^3 + n^3) ≈ O(n^3.5) tipica
 * Complejidad peor caso: O(h * n^4 + n^4) ≈ O(n^4.5)
 *
 * Resultados:
 *   eil51:    ratio=1.007, tiempo=0.042s
 *   berlin52: ratio=1.000, tiempo=0.101s
 *   st70:     ratio=1.011, tiempo=0.073s
 *   eil76:    ratio=1.026, tiempo=0.079s
 *   rat99:    ratio=1.008, tiempo=0.166s
 *   kro200:   ratio=1.004, tiempo=0.609s
 *   a280:     ratio=1.023, tiempo=1.638s
 *   pcb442:   ratio=1.025, tiempo=4.255s
 *
 * Metricas agregadas: Media aritmetica=1.013x | Media geometrica=1.013x | Peor caso=1.026x
 *
 * Conclusion: LK correcto + double-bridge mejorado da los mejores resultados en instancias medianas.
 *   kro200: 1.004x (casi optimo). Pipeline LK + DB + LK escapa optimos locales efectivamente.
 *   Mejor solver O(n^4.5) del proyecto — supera a SolverH1 y compite con SolverE2/G2 (O(n^5))
 *   siendo mas rapido.
 */
class SolverH2 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val neighborLists = buildNeighborLists(instance.points, k = 10)

        // Paso 2: Multi-start selectivo
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

        // Paso 3: LK profundidad 2
        val afterLk = linKernighan(bestTour!!.points, neighborLists)

        // Paso 4: Double-bridge perturbation
        val afterDb = doubleBridgePerturbation(afterLk, maxAttempts = 50)

        // Paso 5: LK final
        val finalTour = linKernighan(afterDb, neighborLists)

        return Tour(points = finalTour)
    }
}
