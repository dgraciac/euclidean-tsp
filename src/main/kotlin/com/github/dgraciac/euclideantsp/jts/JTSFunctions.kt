package com.github.dgraciac.euclideantsp.jts

import com.github.dgraciac.euclideantsp.toJTSPoint
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Point

internal fun lengthAfterInsertBetweenPairOfPoints(
    pair: Pair<Point, Point>,
    unconnectedPoint: Point
): Double {
    return pair.first.distance(unconnectedPoint) + unconnectedPoint.distance(pair.second)
}

internal fun createLinearRing(points: List<Point>): LinearRing = points.plus(points.first()).map { it.coordinate }
    .toTypedArray()
    .let { GeometryFactory().createLinearRing(it) }

internal fun LinearRing.listOfPoints(): List<Point> = coordinates.map { it.toJTSPoint() }

internal fun LinearRing.arrayOfPoints(): Array<Point> = listOfPoints().toTypedArray()
