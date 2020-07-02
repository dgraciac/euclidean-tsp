package com.github.dgraciac.euclideantsp

data class Euclidean2DTSPInstance(val coordinates: List<Point>) {
    init {
        if (coordinates.isEmpty()) throw IllegalArgumentException()
    }
}