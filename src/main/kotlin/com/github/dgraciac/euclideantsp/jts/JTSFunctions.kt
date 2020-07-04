package com.github.dgraciac.euclideantsp.jts

import com.github.dgraciac.euclideantsp.toJTSPoint
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

internal fun Polygon.listOfPoints(): List<Point> = coordinates.map { it.toJTSPoint() }

internal fun Polygon.arrayOfPoints(): Array<Point> = listOfPoints().toTypedArray()
