package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory
import java.util.stream.Stream

/**
 * E008 — Verificacion empirica de la propiedad teorica de capas de convex hull.
 *
 * Hipotesis: En el tour optimo, los vertices de cada capa de convex hull
 * aparecen en el mismo orden ciclico que en su hull.
 *
 * Se verifica con BruteForce en instancias pequeñas (n <= 10) donde el tour
 * optimo es conocido de forma exacta.
 */
internal class ConvexHullLayerOrderTest {
    companion object {
        @JvmStatic
        fun smallInstances(): Stream<Arguments> =
            Stream.of(
                Arguments.of(TRIVIAL),
                Arguments.of(instance4Square),
                Arguments.of(instance4A),
                Arguments.of(instance4B),
                Arguments.of(instance5A),
                Arguments.of(instance5B),
                Arguments.of(instance6A),
                Arguments.of(instance6B),
                Arguments.of(instance6C),
                Arguments.of(instance6D),
                Arguments.of(instance10A),
            )
    }

    @ParameterizedTest
    @MethodSource("smallInstances")
    fun verify_layer_order_preserved_in_optimal_tour(instance: Euclidean2DTSPInstance) {
        // Obtener tour optimo con brute force
        val bruteForce = BruteForce()
        val optimalTour = bruteForce.compute(instance)

        // Calcular capas de convex hull
        val layers = peelConvexHulls(instance.points)

        // Para cada capa, verificar si su orden ciclico se preserva en el tour optimo
        val tourOrder = optimalTour.points.dropLast(1) // Sin duplicado final

        println("Instance: ${instance.name} (${instance.points.size} points, ${layers.size} layers)")
        println("  Optimal tour length: ${optimalTour.length} (expected: ${instance.optimalLength})")

        var allPreserved = true
        for ((layerIdx, layer) in layers.withIndex()) {
            if (layer.size <= 2) {
                println("  Layer $layerIdx: ${layer.size} points — trivial (always preserved)")
                continue
            }

            val preserved = isCyclicOrderPreserved(layer, tourOrder)
            val status = if (preserved) "PRESERVED" else "BROKEN"
            println("  Layer $layerIdx: ${layer.size} points — $status")

            if (!preserved) {
                allPreserved = false
                // Mostrar detalle
                val layerInTourOrder =
                    tourOrder.filter { it in layer.toSet() }
                println("    Hull order:    ${layer.map { "(${it.x},${it.y})" }}")
                println("    Tour order:    ${layerInTourOrder.map { "(${it.x},${it.y})" }}")
            }
        }

        println("  Result: ${if (allPreserved) "ALL LAYERS PRESERVED" else "SOME LAYERS BROKEN"}")
        println()

        // No hacemos assertThat aqui — este test es exploratorio.
        // Si queremos forzar la propiedad, descomenta la linea siguiente:
        // assertThat(allPreserved).isTrue()
    }

    /**
     * Verifica si los puntos de una capa aparecen en el mismo orden ciclico en el tour.
     * El orden puede ser en cualquier direccion (horario o antihorario) y empezar en cualquier punto.
     */
    private fun isCyclicOrderPreserved(
        layerPoints: List<Point>,
        tourOrder: List<Point>,
    ): Boolean {
        if (layerPoints.size <= 2) return true

        // Extraer los puntos de la capa en el orden en que aparecen en el tour
        val layerSet = layerPoints.toSet()
        val inTourOrder = tourOrder.filter { it in layerSet }

        if (inTourOrder.size != layerPoints.size) return false

        // Verificar si inTourOrder es una rotacion ciclica de layerPoints
        // (en cualquiera de las dos direcciones)
        return isCyclicRotation(layerPoints, inTourOrder) ||
            isCyclicRotation(layerPoints.reversed(), inTourOrder)
    }

    /**
     * Verifica si b es una rotacion ciclica de a.
     */
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
