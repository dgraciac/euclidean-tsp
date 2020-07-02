package com.github.dgraciac.euclideantsp.bruteforce

import com.github.dgraciac.euclideantsp.*
import org.locationtech.jts.geom.Coordinate


class BruteForce : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.coordinates.isNotEmpty())

        return instance.coordinates
            .permute()
            .minBy { list: List<Coordinate> -> list.distance() }
            .let {
                Tour(
                    coordinates = it!!,
                    distance = it.distance()
                )
            }
    }
}