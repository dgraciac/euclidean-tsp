package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point

val CUADRADO = Euclidean2DTSPInstance(
    name = "cuadrado",
    optimalLength = 4.0,
    points = listOf(
        Point(0.0 ,0.0),
        Point(1.0 ,0.0),
        Point(0.0 ,1.0),
        Point(1.0 ,1.0)
    )
)
