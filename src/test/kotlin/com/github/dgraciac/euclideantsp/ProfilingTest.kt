package com.github.dgraciac.euclideantsp

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * E038 — Profiling de SolverJ5 para identificar el cuello de botella real.
 * Mide el tiempo de cada fase del pipeline por separado.
 */
internal class ProfilingTest {
    @Test
    fun profile_solverJ5_phases_d657() {
        val instance = D_657
        val n = instance.points.size
        println("=== Profiling SolverJ5 en d657 (n=$n) ===")

        val alphaNl: Map<com.github.dgraciac.euclideantsp.shared.Point, List<com.github.dgraciac.euclideantsp.shared.Point>>
        val distNl: Map<com.github.dgraciac.euclideantsp.shared.Point, List<com.github.dgraciac.euclideantsp.shared.Point>>
        val combinedNl: Map<com.github.dgraciac.euclideantsp.shared.Point, List<com.github.dgraciac.euclideantsp.shared.Point>>

        val t1 =
            measureTimeMillis {
                val a = buildAlphaNearnessList(instance.points, k = 7)
                val d = buildNeighborLists(instance.points, k = 7)
                alphaNl = a
                distNl = d
                combinedNl =
                    instance.points.associateWith { p ->
                        ((a[p] ?: emptyList()) + (d[p] ?: emptyList())).distinct()
                    }
            }
        println("1. Build candidates (α+dist): ${t1}ms")

        // Multi-start (hull + constructions)
        val hull =
            org.locationtech.jts.algorithm
                .ConvexHull(
                    instance.points.map { it.toCoordinate() }.toTypedArray(),
                    org.locationtech.jts.geom
                        .GeometryFactory(),
                ).convexHull
        val startPoints =
            hull.coordinates.dropLast(1).map { coord ->
                instance.points.first { it.x == coord.x && it.y == coord.y }
            }
        println("   Hull vertices: ${startPoints.size}")

        var bestTour: com.github.dgraciac.euclideantsp.shared.Tour? = null
        val t2 =
            measureTimeMillis {
                for (sp in startPoints) {
                    val nn = nearestNeighborFrom(instance.points, sp)
                    val opt = twoOptWithNeighborLists(nn, combinedNl)
                    val tour =
                        com.github.dgraciac.euclideantsp.shared
                            .Tour(points = opt)
                    if (bestTour == null || tour.length < bestTour!!.length) bestTour = tour
                }
                val constructions =
                    listOf(
                        farthestInsertion(instance.points),
                        convexHullInsertion(instance.points),
                        peelingInsertion(instance.points),
                        greedyConstruction(instance.points),
                    )
                for (c in constructions) {
                    val opt = twoOptWithNeighborLists(c, combinedNl)
                    val tour =
                        com.github.dgraciac.euclideantsp.shared
                            .Tour(points = opt)
                    if (bestTour == null || tour.length < bestTour!!.length) bestTour = tour
                }
            }
        println("2. Multi-start (${startPoints.size} hull + 4 constr) + 2-opt-nl: ${t2}ms")

        val t3 =
            measureTimeMillis {
                bestTour =
                    com.github.dgraciac.euclideantsp.shared
                        .Tour(points = orOpt(bestTour!!.points))
            }
        println("3. Or-opt: ${t3}ms")

        val t4 =
            measureTimeMillis {
                bestTour =
                    com.github.dgraciac.euclideantsp.shared
                        .Tour(points = twoOpt(bestTour!!.points))
            }
        println("4. 2-opt completo: ${t4}ms")

        // Rama A: LK-2 + DB + LK-2
        var tourA: com.github.dgraciac.euclideantsp.shared.Tour? = null
        val t5a =
            measureTimeMillis {
                val lk = linKernighan(bestTour!!.points, combinedNl)
                val db = doubleBridgePerturbation(lk, maxAttempts = 20)
                tourA =
                    com.github.dgraciac.euclideantsp.shared
                        .Tour(points = linKernighan(db, combinedNl))
            }
        println("5a. Rama LK-2 + DB + LK-2: ${t5a}ms")

        // Rama B: LK-deep + DB + LK-deep
        var tourB: com.github.dgraciac.euclideantsp.shared.Tour? = null
        val t5b =
            measureTimeMillis {
                val lk = linKernighanDeep(bestTour!!.points, combinedNl, maxDepth = 5)
                val db = doubleBridgePerturbation(lk, maxAttempts = 20)
                tourB =
                    com.github.dgraciac.euclideantsp.shared
                        .Tour(points = linKernighanDeep(db, combinedNl, maxDepth = 5))
            }
        println("5b. Rama LK-deep(5) + DB + LK-deep(5): ${t5b}ms")

        val best = if (tourA!!.length <= tourB!!.length) tourA!! else tourB!!
        println("Total: ${t1 + t2 + t3 + t4 + t5a + t5b}ms")
        println("Ratio: ${"%.4f".format(best.length / instance.optimalLength)}x")
    }
}
