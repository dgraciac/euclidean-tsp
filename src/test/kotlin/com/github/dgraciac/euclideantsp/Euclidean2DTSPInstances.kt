package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate

internal object Euclidean2DTSPInstances {
    val INSTANCE_1 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 1.0),
            Coordinate(1.0, 0.0)
        )
    )
    val INSTANCE_2 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 0.0),
            Coordinate(2.0, 0.0),
            Coordinate(1.0, 2.0)
        )
    )
    val INSTANCE_3 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(1.0, 3.0),
            Coordinate(1.0, 0.0),
            Coordinate(0.0, 2.0),
            Coordinate(2.0, 4.0),
            Coordinate(3.0, 3.0)
        )
    )
    val INSTANCE_4 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(3.0, 0.0),
            Coordinate(4.0, 5.0),
            Coordinate(3.0, 3.0),
            Coordinate(0.0, 4.0),
            Coordinate(10.0, 4.0),
            Coordinate(4.0, 6.0)
        )
    )
    val INSTANCE_5 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 4.0),
            Coordinate(7.0, 0.0),
            Coordinate(7.0, 4.0),
            Coordinate(3.0, 2.0),
            Coordinate(4.0, 2.0)
        )
    )
    val INSTANCE_6 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(0.0, 4.0),
            Coordinate(7.0, 0.0),
            Coordinate(7.0, 4.0),
            Coordinate(1.0, 2.0),
            Coordinate(6.0, 2.0)
        )
    )
    val INSTANCE_7 = Euclidean2DTSPInstance(
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(18.0, 0.0),
            Coordinate(9.0, 18.0),
            Coordinate(2.0, 1.0),
            Coordinate(3.0, 1.0),
            Coordinate(3.0, 2.0)
        )
    )
}
