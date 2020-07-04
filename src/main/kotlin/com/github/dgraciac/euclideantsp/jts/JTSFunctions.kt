package com.github.dgraciac.euclideantsp.jts

import org.locationtech.jts.geom.Point

internal fun lengthAfterInsertBetweenPairOfPoints(
    pair: Pair<Point, Point>,
    unconnectedPoint: Point
): Double {
    return pair.first.distance(unconnectedPoint) + unconnectedPoint.distance(pair.second)
}