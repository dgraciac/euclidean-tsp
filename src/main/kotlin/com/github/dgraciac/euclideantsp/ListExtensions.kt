package com.github.dgraciac.euclideantsp

import java.security.InvalidAlgorithmParameterException

fun <E> List<E>.permute(): List<List<E>> {
    return when (this.size) {
        0 -> throw InvalidAlgorithmParameterException()
        1 -> listOf(this)
        else -> this.map { element: E ->
            this.minusElement(element).permute().map { subPermutation: List<E> ->
                listOf(element).plus(subPermutation)
            }
        }.reduce { first, second -> first.plus(second) }
    }
}
