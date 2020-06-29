package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate
import java.lang.IllegalArgumentException

data class Euclidean2DTSPInstance(val coordinates: List<Coordinate>) {
    init {
        if (coordinates.isEmpty()) throw IllegalArgumentException()
    }
}