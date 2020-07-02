package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate

fun Point.toCoordinate(): Coordinate = Coordinate(x, y)

fun Point.distance(point2: Point): Double = toCoordinate().distance(point2.toCoordinate())

fun List<Point>.length(): Double = map { it.toCoordinate() }.length()