package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate

fun List<Coordinate>.length(): Double {
    return this.plus(this.first())
        .zipWithNext { first: Coordinate, second: Coordinate ->
            first.distance(second)
        }.reduce { acc: Double, d: Double -> acc + d }
}