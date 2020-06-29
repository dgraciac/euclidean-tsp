package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate

fun <E> List<E>.permute(): List<List<E>> {
    return when (this.size) {
        0 -> throw IllegalArgumentException()
        1 -> listOf(this)
        else -> this.map { element: E ->
            this.minusElement(element).permute().map { subPermutation: List<E> ->
                listOf(element).plus(subPermutation)
            }
        }.reduce { first, second -> first.plus(second) }
    }
}

fun List<Coordinate>.distance(): Double {
    return this.plus(this.first())
        .zipWithNext { first: Coordinate, second: Coordinate ->
            first.distance(second)
        }.reduce { acc: Double, d: Double -> acc + d }
}