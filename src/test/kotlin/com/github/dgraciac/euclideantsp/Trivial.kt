package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point

val TRIVIAL = Euclidean2DTSPInstance(
    name = "trivial",
    optimalLength = 3.414213562373095,
    points = listOf(
        Point(0.0 ,0.0),
        Point(0.0 ,1.0),
        Point(1.0 ,0.0)
    )
)
