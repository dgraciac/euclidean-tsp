package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverJ7 — SolverJ5 + LK no secuencial (bidireccional)
 *
 * Linea de investigacion: J (busqueda local avanzada)
 * Padre: SolverJ5
 * Experimento: E036
 *
 * Hipotesis: LK con movimientos no secuenciales (romper aristas en ambas
 * direcciones del tour) integra double-bridge directamente en la busqueda LK,
 * encontrando mejoras que el LK secuencial + double-bridge por separado no puede.
 *
 * Algoritmo: Igual que SolverJ5 pero con LK-deep no secuencial en ambas ramas.
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — LK-deep tiene 2*K^5 constante por mejora (factor 2 por bidireccional)
 *
 * Resultados:
 *   eil51:    ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   berlin52: ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   st70:     ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   eil76:    ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   rat99:    ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   kro200:   ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   a280:     ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *   pcb442:   ratio=Identico a SolverJ5, tiempo=Identico a SolverJ5
 *
 * Metricas agregadas: Identico a SolverJ5 en todas las instancias
 *
 * Conclusion: LK no secuencial (bidireccional) NO aporta mejora sobre LK secuencial + DB.
 * El double-bridge explicito ya cubre los movimientos que LK bidireccional buscaria.
 * La combinacion LK secuencial + double-bridge es suficiente.
 */
class SolverJ7 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Multi-start rapido
        val coordinates = instance.points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val startPoints =
            hullCoords.map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }

        var baseTour: Tour? = null
        for (startPoint in startPoints) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        val constructions =
            listOf(
                farthestInsertion(instance.points),
                convexHullInsertion(instance.points),
                peelingInsertion(instance.points),
                greedyConstruction(instance.points),
            )
        for (construction in constructions) {
            val afterTwoOptNl = twoOptWithNeighborLists(construction, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (baseTour == null || tour.length < baseTour.length) {
                baseTour = tour
            }
        }

        // Fase comun: or-opt + 2-opt
        val afterOrOpt = orOpt(baseTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)

        // Rama A: LK-2 secuencial + DB + LK-2
        val afterLk2 = linKernighan(afterTwoOpt, combinedNl)
        val afterDb2 = doubleBridgePerturbation(afterLk2, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, combinedNl))

        // Rama B: LK-deep(5) no secuencial + DB + LK-deep(5)
        val afterLkNs = linKernighanDeep(afterTwoOpt, combinedNl, maxDepth = 5)
        val afterDbNs = doubleBridgePerturbation(afterLkNs, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDbNs, combinedNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
