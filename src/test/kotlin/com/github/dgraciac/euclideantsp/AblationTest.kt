package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.api.Test
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory
import kotlin.system.measureTimeMillis

/**
 * E040 — Ablacion: aislar que componente de K1 causa la mejora en n>700.
 *
 * Variantes:
 * A. J5 con multi-start completo (n starts) en vez de hull — aisla efecto de mas starts
 * B. J5 con DB ligero (2-opt-nl) en vez de pesado — aisla efecto del DB
 * C. K1 con DB pesado — aisla si K1 mejoraria con DB pesado
 */
internal class AblationTest {
    @Test
    fun ablation_rat783() {
        val instance = RAT_783
        println("=== Ablacion en rat783 (n=${instance.points.size}) ===")
        println("J5 referencia: 1.031x | K1 referencia: 1.026x")

        val alphaNl = buildAlphaNearnessList(instance.points, k = 7)
        val distNl = buildNeighborLists(instance.points, k = 7)
        val combinedNl =
            instance.points.associateWith { p ->
                ((alphaNl[p] ?: emptyList()) + (distNl[p] ?: emptyList())).distinct()
            }

        // Variante A: J5 con multi-start completo (n starts)
        var tourA: Tour? = null
        val timeA =
            measureTimeMillis {
                var best: Tour? = null
                for (sp in instance.points) {
                    val nn = nearestNeighborFrom(instance.points, sp)
                    val opt = twoOptWithNeighborLists(nn, combinedNl)
                    val t = Tour(points = opt)
                    if (best == null || t.length < best.length) best = t
                }
                // Pipeline J5: or-opt + 2-opt + LK + DB pesado + LK
                val o = orOpt(best!!.points)
                val t2 = twoOpt(o)
                val lk = linKernighan(t2, combinedNl)
                val db = doubleBridgePerturbation(lk, maxAttempts = 20)
                tourA = Tour(points = linKernighan(db, combinedNl))
            }
        println("A. J5 + multi-start completo: ${"%.4f".format(tourA!!.length / instance.optimalLength)}x (${timeA}ms)")

        // Variante B: J5 con DB ligero (2-opt-nl en DB)
        var tourB: Tour? = null
        val timeB =
            measureTimeMillis {
                val hull =
                    ConvexHull(
                        instance.points.map { it.toCoordinate() }.toTypedArray(),
                        GeometryFactory(),
                    ).convexHull
                val startPoints =
                    hull.coordinates.dropLast(1).map { coord ->
                        instance.points.first { it.x == coord.x && it.y == coord.y }
                    }
                var best: Tour? = null
                for (sp in startPoints) {
                    val nn = nearestNeighborFrom(instance.points, sp)
                    val opt = twoOptWithNeighborLists(nn, combinedNl)
                    val t = Tour(points = opt)
                    if (best == null || t.length < best.length) best = t
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
                    val t = Tour(points = opt)
                    if (best == null || t.length < best.length) best = t
                }
                // Pipeline con DB ligero
                val o = orOpt(best!!.points)
                val t2 = twoOpt(o)
                val lk = linKernighan(t2, combinedNl)
                // DB ligero: 2-opt-nl en vez de 2-opt completo
                val dbResult = dbLightweight(lk, combinedNl, 30)
                tourB = Tour(points = linKernighan(dbResult, combinedNl))
            }
        println("B. J5 + DB ligero: ${"%.4f".format(tourB!!.length / instance.optimalLength)}x (${timeB}ms)")

        // Variante C: solo multi-start completo + 2-opt-nl (sin pipeline profundo)
        var tourC: Tour? = null
        val timeC =
            measureTimeMillis {
                var best: Tour? = null
                for (sp in instance.points) {
                    val nn = nearestNeighborFrom(instance.points, sp)
                    val opt = twoOptWithNeighborLists(nn, combinedNl)
                    val t = Tour(points = opt)
                    if (best == null || t.length < best.length) best = t
                }
                tourC = best
            }
        println(
            "C. Solo multi-start completo + 2-opt-nl (sin pipeline): ${"%.4f".format(
                tourC!!.length / instance.optimalLength,
            )}x (${timeC}ms)",
        )
    }

    private fun dbLightweight(
        tourPoints: List<Point>,
        neighborLists: Map<Point, List<Point>>,
        maxAttempts: Int,
    ): List<Point> {
        val points = tourPoints.dropLast(1)
        val n = points.size
        if (n < 8) return tourPoints

        var bestTour = tourPoints
        var bestLength = points.indices.sumOf { points[it].distance(points[(it + 1) % n]) }

        val candidates =
            (0 until n)
                .map { i -> Pair(i, points[i].distance(points[(i + 1) % n])) }
                .sortedByDescending { it.second }
                .take(minOf(12, n))
                .map { it.first }
                .sorted()

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

                    val perturbed =
                        (
                            (0..i1).map { points[it] } +
                                (i2 + 1..i3).map { points[it] } +
                                (i1 + 1..i2).map { points[it] } +
                                (i3 + 1 until n).map { points[it] }
                        ).toMutableList()
                    perturbed.add(perturbed.first())

                    val opt1 = twoOptWithNeighborLists(perturbed, neighborLists)
                    val opt2 = orOpt(opt1)
                    val opt3 = twoOptWithNeighborLists(opt2, neighborLists)

                    val len = opt3.dropLast(1).let { pts -> pts.indices.sumOf { pts[it].distance(pts[(it + 1) % pts.size]) } }
                    if (len < bestLength - 1e-10) {
                        bestTour = opt3
                        bestLength = len
                    }
                    attempts++
                }
            }
        }
        return bestTour
    }
}
