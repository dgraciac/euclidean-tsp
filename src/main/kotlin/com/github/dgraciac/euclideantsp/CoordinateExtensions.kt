package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory

/**
 * Calcula la longitud total de un tour representado como lista de Coordinates.
 * Añade el primer elemento al final para cerrar el ciclo, luego suma distancias consecutivas.
 * Complejidad: O(n) donde n = tamaño de la lista
 */
fun List<Coordinate>.length(): Double =
    this
        .plus(this.first())
        .zipWithNext { first: Coordinate, second: Coordinate ->
            first.distance(second)
        }.reduce { acc: Double, d: Double -> acc + d }

/** Convierte un Coordinate de JTS a un Point de JTS usando GeometryFactory. Complejidad: O(1) */
fun Coordinate.toJTSPoint(): org.locationtech.jts.geom.Point = GeometryFactory().createPoint(this)
