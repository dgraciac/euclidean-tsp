package com.github.dgraciac.euclideantsp.jts

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.toCoordinate
import com.github.dgraciac.euclideantsp.toJTSPoint
import org.locationtech.jts.algorithm.ConvexHull
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

internal fun List<Point>.toLinearRing(): LinearRing =
    GeometryFactory().createLinearRing(listOfCoordinates().toTypedArray())

internal fun LinearRing.listOfPoints(): List<Point> = coordinates.map { it.toJTSPoint() }

internal fun LinearRing.arrayOfPoints(): Array<Point> = listOfPoints().toTypedArray()

internal fun ArrayList<Point>.areLinearRing(): Boolean = kotlin.runCatching { toLinearRing() }.fold(
    onFailure = { false },
    onSuccess = { it.isClosedSimpleAndValid() }
)

internal fun LinearRing.isClosedSimpleAndValid(): Boolean = isClosed.and(isSimple).and(isValid)

internal fun List<Point>.listOfCoordinates(): List<Coordinate> = map { it.coordinate }

internal fun Euclidean2DTSPInstance.centroid(): Point =
    ConvexHull(points.map { it.toCoordinate() }.toTypedArray(), GeometryFactory()).convexHull.centroid
