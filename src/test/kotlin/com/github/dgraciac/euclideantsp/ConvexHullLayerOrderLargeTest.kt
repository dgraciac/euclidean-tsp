package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * E011 — Verificacion de la propiedad de capas en instancias grandes.
 *
 * No podemos usar BruteForce en instancias grandes, asi que verificamos
 * si los mejores tours heuristicos (SolverC3, SolverB3, SolverE1) respetan
 * el orden ciclico de cada capa de convex hull.
 *
 * Si los tours heuristicos lo respetan, es evidencia adicional de que la propiedad
 * es natural en tours de alta calidad. Si no lo respetan, nos dice donde la
 * busqueda local rompe el orden de las capas.
 */
internal class ConvexHullLayerOrderLargeTest {
    companion object {
        val solvers: List<Pair<String, Euclidean2DTSPSolver>> =
            listOf(
                "SolverC3" to SolverC3(),
                "SolverB3" to SolverB3(),
                "SolverE1" to SolverE1(),
            )
    }

    @ParameterizedTest
    @ArgumentsSource(TSPInstanceProvider::class)
    fun verify_layer_order_in_heuristic_tours(instance: Euclidean2DTSPInstance) {
        val layers = peelConvexHulls(instance.points)

        println("Instance: ${instance.name} (${instance.points.size} points, ${layers.size} layers)")
        println(
            "  Layers: ${layers.mapIndexed { i, l -> "L$i(${l.size})" }.joinToString(", ")}",
        )

        solvers.forEach { (name, solver) ->
            val tour = solver.compute(instance)
            val tourOrder = tour.points.dropLast(1)

            var preserved = 0
            var broken = 0
            var trivial = 0
            val brokenLayers = mutableListOf<String>()

            for ((layerIdx, layer) in layers.withIndex()) {
                if (layer.size <= 2) {
                    trivial++
                    continue
                }

                if (isCyclicOrderPreserved(layer, tourOrder)) {
                    preserved++
                } else {
                    broken++
                    brokenLayers.add("L$layerIdx(${layer.size})")
                }
            }

            val total = preserved + broken + trivial
            val status = if (broken == 0) "ALL PRESERVED" else "BROKEN: ${brokenLayers.joinToString(", ")}"
            println(
                "  $name: ratio=${
                    "%.4f".format(tour.length / instance.optimalLength)
                } — $preserved/$total preserved, $broken broken, $trivial trivial — $status",
            )
        }
        println()
    }

    private fun isCyclicOrderPreserved(
        layerPoints: List<Point>,
        tourOrder: List<Point>,
    ): Boolean {
        if (layerPoints.size <= 2) return true
        val layerSet = layerPoints.toSet()
        val inTourOrder = tourOrder.filter { it in layerSet }
        if (inTourOrder.size != layerPoints.size) return false
        return isCyclicRotation(layerPoints, inTourOrder) ||
            isCyclicRotation(layerPoints.reversed(), inTourOrder)
    }

    private fun isCyclicRotation(
        a: List<Point>,
        b: List<Point>,
    ): Boolean {
        if (a.size != b.size) return false
        val n = a.size
        val doubled = a + a
        for (offset in 0 until n) {
            if ((0 until n).all { doubled[offset + it] == b[it] }) return true
        }
        return false
    }

    private fun peelConvexHulls(points: Set<Point>): List<List<Point>> {
        val layers = mutableListOf<List<Point>>()
        val remaining = points.toMutableSet()

        while (remaining.size >= 3) {
            val coordinates = remaining.map { it.toCoordinate() }.toTypedArray()
            val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
            val hullCoordinates = hull.coordinates.dropLast(1)
            val hullPoints =
                hullCoordinates.map { coord ->
                    remaining.first { it.x == coord.x && it.y == coord.y }
                }

            if (hullPoints.size < 3) {
                layers.add(remaining.toList())
                remaining.clear()
            } else {
                layers.add(hullPoints)
                remaining.removeAll(hullPoints.toSet())
            }
        }

        if (remaining.isNotEmpty()) {
            layers.add(remaining.toList())
        }

        return layers
    }
}
