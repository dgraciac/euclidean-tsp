package com.github.dgraciac.euclideantsp.jts

import com.github.dgraciac.euclideantsp.toJTSPoint
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Point

internal fun lengthAfterInsertBetweenPairOfPoints(
    first: Point,
    second: Point,
    unconnectedPoint: Point
): Double {
    return first.distance(unconnectedPoint) + unconnectedPoint.distance(second)
}

internal fun createLinearRing(points: List<Point>): LinearRing = points.plus(points.first()).listOfCoordinates()
    .toTypedArray()
    .let { GeometryFactory().createLinearRing(it) }

internal fun LinearRing.listOfPoints(): List<Point> = coordinates.map { it.toJTSPoint() }

internal fun LinearRing.arrayOfPoints(): Array<Point> = listOfPoints().toTypedArray()

internal fun ArrayList<Point>.toLinearRing(): LinearRing =
    GeometryFactory().createLinearRing(plus(first()).listOfCoordinates().toTypedArray())

internal fun ArrayList<Point>.areLinearRing(): Boolean = kotlin.runCatching { toLinearRing() }.fold(
    onFailure = { false },
    onSuccess = { it.isClosedSimpleAndValid() }
)

internal fun LinearRing.isClosedSimpleAndValid(): Boolean = isClosed.and(isSimple).and(isValid)

internal fun List<Point>.listOfCoordinates(): List<Coordinate> = map { it.coordinate }
