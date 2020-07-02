package com.github.dgraciac.euclideantsp

data class Tour(val points: List<Point>) {
    val length: Double = points.length()
}

