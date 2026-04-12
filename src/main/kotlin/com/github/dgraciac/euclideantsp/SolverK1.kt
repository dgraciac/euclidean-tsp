package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverK1 — SolverJ5 optimizado: mas multi-start + DB ligero con 2-opt-nl
 *
 * Linea de investigacion: K (optimizacion de rendimiento para mejor aproximacion)
 * Padre: SolverJ5
 * Experimento: E038
 *
 * Hipotesis: El profiling de SolverJ5 muestra que 60% del tiempo se gasta en
 * LK-deep + DB (double-bridge con re-optimizacion pesada). Si reemplazamos la
 * re-optimizacion del DB por 2-opt-nl (10x mas rapido) y usamos el tiempo
 * ahorrado para hacer multi-start con ALL n puntos (no solo hull), la mayor
 * diversidad de starts mejorara la aproximacion.
 *
 * Cambios respecto a SolverJ5:
 * 1. Multi-start con TODOS los puntos (n starts) usando solo 2-opt-nl rapido
 * 2. DB usa 2-opt-nl en vez de 2-opt completo para re-optimizar
 * 3. Solo rama LK-2 (sin LK-deep que es 2x mas lento)
 *
 * Complejidad e2e: O(n^3) — n starts × O(n^2) 2-opt-nl + O(n^3) pipeline en el mejor
 * Complejidad peor caso: O(n^3)
 *
 * Resultados:
 *   eil51:    ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   berlin52: ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   st70:     ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   eil76:    ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   rat99:    ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   kro200:   ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   a280:     ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   pcb442:   ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   d657:     ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   rat783:   ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   pr1002:   ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   d1291:    ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *   d2103:    ratio=ver comparativa en RESEARCH_LOG E038, tiempo=ver comparativa en RESEARCH_LOG E038
 *
 * Metricas agregadas: Gana en instancias grandes (rat783 1.026x, d1291 1.038x) pero pierde en medianas.
 *
 * Conclusion: Multi-start completo (n starts) mejora en instancias grandes (n>700) gracias
 * a mayor diversidad. Pero el DB ligero (2-opt-nl) pierde calidad vs DB pesado (2-opt completo)
 * en instancias medianas. No hay un solver unico que gane en todas las instancias.
 */
class SolverK1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Fase 1: Multi-start COMPLETO (todos los puntos) con 2-opt-nl rapido
        var bestTour: Tour? = null
        for (startPoint in instance.points) {
            val nnTour = nearestNeighborFrom(instance.points, startPoint)
            val afterTwoOptNl = twoOptWithNeighborLists(nnTour, combinedNl)
            val tour = Tour(points = afterTwoOptNl)
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Tambien probar construcciones diversas
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
            if (bestTour == null || tour.length < bestTour.length) {
                bestTour = tour
            }
        }

        // Fase 2: Busqueda profunda en el mejor tour
        val afterOrOpt = orOpt(bestTour!!.points)
        val afterTwoOpt = twoOpt(afterOrOpt)
        val afterLk = linKernighan(afterTwoOpt, combinedNl)

        // Fase 3: DB ligero — re-optimiza con 2-opt-nl en vez de 2-opt completo
        val afterDb = doubleBridgeLightweight(afterLk, combinedNl, maxAttempts = 30)
        val finalTour = linKernighan(afterDb, combinedNl)

        return Tour(points = finalTour)
    }

    /**
     * Double-bridge con re-optimizacion ligera (2-opt-nl en vez de 2-opt completo).
     * Mas rapido que doubleBridgePerturbation, permitiendo mas intentos.
     * Complejidad: O(maxAttempts * n^2) en vez de O(maxAttempts * n^3)
     */
    private fun doubleBridgeLightweight(
        tourPoints: List<Point>,
        neighborLists: Map<Point, List<Point>>,
        maxAttempts: Int,
    ): List<Point> {
        val points = tourPoints.dropLast(1)
        val n = points.size
        if (n < 8) return tourPoints

        var bestTour = tourPoints
        var bestLength = points.indices.sumOf { points[it].distance(points[(it + 1) % n]) }

        val edgesByLength =
            (0 until n)
                .map { i -> Pair(i, points[i].distance(points[(i + 1) % n])) }
                .sortedByDescending { it.second }

        val candidates = edgesByLength.take(minOf(12, n)).map { it.first }.sorted()

        var attempts = 0
        for (a in candidates.indices) {
            if (attempts >= maxAttempts) break
            for (b in a + 1 until candidates.size) {
                if (attempts >= maxAttempts) break
                for (c in b + 1 until candidates.size) {
                    if (attempts >= maxAttempts) break

                    val i1 = candidates[a]
                    val i2 = candidates[b]
                    val i3 = candidates[c]

                    if (i2 - i1 < 1 || i3 - i2 < 1 || n - i3 < 1) continue

                    val seg1 = (0..i1).map { points[it] }
                    val seg2 = (i1 + 1..i2).map { points[it] }
                    val seg3 = (i2 + 1..i3).map { points[it] }
                    val seg4 = (i3 + 1 until n).map { points[it] }

                    val perturbed = (seg1 + seg3 + seg2 + seg4).toMutableList()
                    perturbed.add(perturbed.first())

                    // Re-optimizar con 2-opt-nl (rapido) + or-opt
                    val afterTwoOptNl = twoOptWithNeighborLists(perturbed, neighborLists)
                    val afterOrOpt = orOpt(afterTwoOptNl)
                    val finalTour = twoOptWithNeighborLists(afterOrOpt, neighborLists)

                    val finalLength =
                        finalTour.dropLast(1).let { pts ->
                            pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) }
                        }

                    if (finalLength < bestLength - 1e-10) {
                        bestTour = finalTour
                        bestLength = finalLength
                    }

                    attempts++
                }
            }
        }

        return bestTour
    }
}
