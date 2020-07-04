package com.github.dgraciac.euclideantsp.jts

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

internal fun lengthAfterInsertBetweenPairOfPoints(
    pair: Pair<Point, Point>,
    unconnectedPoint: Point
): Double {
    return pair.first.distance(unconnectedPoint) + unconnectedPoint.distance(pair.second)
}

internal fun createPolygon(points: List<Point>): Polygon = points.plus(points.first()).map { it.coordinate }
    .toTypedArray()
    .let { GeometryFactory().createPolygon(it) }
