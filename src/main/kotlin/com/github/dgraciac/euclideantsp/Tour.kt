package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate

data class Tour(val coordinates: List<Coordinate>, val distance: Double)