package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import org.locationtech.jts.geom.Coordinate

/** Convierte un Point del dominio a un Coordinate de JTS. Complejidad: O(1) */
fun Point.toCoordinate(): Coordinate = Coordinate(x, y)

/** Calcula la distancia euclidea entre dos puntos. Complejidad: O(1) */
fun Point.distance(point2: Point): Double = toCoordinate().distance(point2.toCoordinate())

/** Convierte un Point del dominio a un Point de JTS. Complejidad: O(1) */
fun Point.toJTSPoint(): org.locationtech.jts.geom.Point = toCoordinate().toJTSPoint()

/**
 * Calcula la longitud total de un tour (lista de puntos).
 * Suma las distancias entre puntos consecutivos, incluyendo el cierre (ultimo -> primero).
 * Complejidad: O(n) donde n = tamaño de la lista
 */
fun List<Point>.length(): Double = map(Point::toCoordinate).length()

/** Convierte un conjunto de Points del dominio a Coordinates de JTS. Complejidad: O(n) */
fun Set<Point>.toCoordinates(): Set<Coordinate> = toList().map(Point::toCoordinate).toSet()

/** Convierte un conjunto de Points del dominio a Points de JTS. Complejidad: O(n) */
fun Set<Point>.toJTSPoints(): Set<org.locationtech.jts.geom.Point> = map { it.toJTSPoint() }.toSet()
