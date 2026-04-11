package com.github.dgraciac.euclideantsp.jts

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.toCoordinate
import com.github.dgraciac.euclideantsp.toJTSPoint
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Point

/**
 * Calcula la distancia del camino first -> unconnectedPoint -> second.
 * Complejidad: O(1)
 */
internal fun lengthAfterInsertBetweenPairOfPoints(
    first: Point,
    second: Point,
    unconnectedPoint: Point,
): Double = first.distance(unconnectedPoint) + unconnectedPoint.distance(second)

/** Construye un LinearRing (anillo cerrado) de JTS a partir de una lista de puntos. Complejidad: O(n) */
internal fun List<Point>.toLinearRing(): LinearRing = GeometryFactory().createLinearRing(listOfCoordinates().toTypedArray())

/** Construye un LineString de JTS a partir de una lista de puntos. Complejidad: O(n) */
internal fun List<Point>.toLineString(): LineString = GeometryFactory().createLineString(listOfCoordinates().toTypedArray())

/** Extrae la lista de puntos JTS de una geometria. Complejidad: O(n) */
internal fun Geometry.listOfPoints(): List<Point> = coordinates.map { it.toJTSPoint() }

/** Extrae un array de puntos JTS de una geometria. Complejidad: O(n) */
internal fun Geometry.arrayOfPoints(): Array<Point> = listOfPoints().toTypedArray()

/**
 * Verifica si los puntos forman un LinearRing valido (cerrado, simple, sin auto-intersecciones).
 * Construye el LinearRing y valida sus propiedades geometricas.
 * Complejidad: O(n) — construccion del ring O(n) + validacion de simplicidad O(n)
 */
internal fun ArrayList<Point>.isLinearRing(): Boolean =
    kotlin.runCatching { toLinearRing() }.fold(
        onFailure = { false },
        onSuccess = { it.isClosedSimpleAndValid() },
    )

/** Verifica que el LinearRing esta cerrado, es valido y es simple (sin auto-intersecciones). Complejidad: O(n) */
internal fun LinearRing.isClosedSimpleAndValid(): Boolean = isClosedAndValid().and(isSimple)

/** Verifica que el LinearRing esta cerrado y es valido. Complejidad: O(1) */
internal fun LinearRing.isClosedAndValid(): Boolean = isClosed.and(isValid)

/** Extrae las coordenadas de una lista de puntos JTS. Complejidad: O(n) */
internal fun List<Point>.listOfCoordinates(): List<Coordinate> = map { it.coordinate }

/**
 * Calcula el centroide de la instancia TSP usando el convex hull.
 * Complejidad: O(n log n) — dominada por el calculo del convex hull
 */
internal fun Euclidean2DTSPInstance.centroid(): Point =
    ConvexHull(points.map { it.toCoordinate() }.toTypedArray(), GeometryFactory()).convexHull.centroid

/**
 * Encuentra la mejor posicion para insertar un punto en el tour actual.
 * Criterio: minimiza la distancia absoluta d(first, point) + d(point, second).
 * Para cada arista (i, i+1), prueba insertar el punto y verifica que el resultado
 * siga siendo un LinearRing valido.
 *
 * @param point el punto a insertar
 * @return par (indice de insercion, distancia minima)
 * Complejidad: O(n^2) — itera n aristas, cada una con validacion isLinearRing O(n)
 */
internal fun ArrayList<Point>.findBestIndexToInsertAt(point: Point): Pair<Int, Double> {
    var bestIndexToInsertAt: Int = -1
    var minimumLength: Double = Double.POSITIVE_INFINITY

    for (i: Int in 0 until this.size - 1) {
        add(i + 1, point)
        val isLinearRing: Boolean = isLinearRing().also { this.removeAt(i + 1) }
        if (isLinearRing) {
            val first: Point = this[i]
            val second: Point = this[i + 1]
            lengthAfterInsertBetweenPairOfPoints(first, second, point)
                .let { length: Double ->
                    if (length < minimumLength) {
                        bestIndexToInsertAt = i + 1
                        minimumLength = length
                    }
                }
        }
    }

    if (bestIndexToInsertAt == -1) throw RuntimeException("Best Index is null")
    return Pair(bestIndexToInsertAt, minimumLength)
}

/**
 * Encuentra la mejor posicion para insertar un punto en el tour actual.
 * Criterio: minimiza el ratio (d(first, point) + d(point, second)) / d(first, second).
 * Este criterio penaliza menos la insercion en aristas largas (donde hay mas holgura)
 * y prefiere insertar donde el desvio relativo es menor.
 *
 * @param point el punto a insertar
 * @return par (indice de insercion, ratio minimo)
 * Complejidad: O(n^2) — itera n aristas, cada una con validacion isLinearRing O(n)
 */
internal fun ArrayList<Point>.findBestIndexToInsertAt2(point: Point): Pair<Int, Double> {
    var bestIndexToInsertAt: Int = -1
    var minimumRatio: Double = Double.POSITIVE_INFINITY

    for (i: Int in 0 until this.size - 1) {
        add(i + 1, point)
        val isLinearRing: Boolean = isLinearRing().also { this.removeAt(i + 1) }
        if (isLinearRing) {
            val first: Point = this[i]
            val second: Point = this[i + 1]
            lengthAfterInsertBetweenPairOfPoints(first, second, point)
                .let { length: Double ->
                    val ratio: Double = length / first.distance(second)
                    if (ratio < minimumRatio) {
                        bestIndexToInsertAt = i + 1
                        minimumRatio = ratio
                    }
                }
        }
    }

    if (bestIndexToInsertAt == -1) throw RuntimeException("Best Index is null")
    return Pair(bestIndexToInsertAt, minimumRatio)
}
