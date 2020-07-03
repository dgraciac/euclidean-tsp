package com.github.dgraciac.euclideantsp.shared

data class Euclidean2DTSPInstance(
    val name: String = "unnamed",
    val points: List<Point>,
    val optimalLength: Double
) {
    init {
        require(points.isNotEmpty())
    }
}
