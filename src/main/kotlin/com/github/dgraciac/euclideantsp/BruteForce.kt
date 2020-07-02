package com.github.dgraciac.euclideantsp

class BruteForce : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.coordinates.isNotEmpty())

        return instance.coordinates
            .permute()
            .minBy { list: List<Point> -> list.length() }
            .let { Tour(points = it!!) }
    }
}