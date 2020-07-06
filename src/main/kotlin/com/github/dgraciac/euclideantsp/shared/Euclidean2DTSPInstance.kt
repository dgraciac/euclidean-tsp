package com.github.dgraciac.euclideantsp.shared

open class Euclidean2DTSPInstance(
    val name: String,
    val points: Set<Point>,
    val optimalLength: Double
) {
    init {
        require(points.isNotEmpty())
    }
}
