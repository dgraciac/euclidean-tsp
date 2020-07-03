package com.github.dgraciac.euclideantsp

fun <E> List<E>.permute(): List<List<E>> {
    require(this.isNotEmpty())
    return when (this.size) {
        1 -> listOf(this)
        else -> this.map { element: E ->
            this.minusElement(element).permute().map { subPermutation: List<E> ->
                listOf(element).plus(subPermutation)
            }
        }.reduce { first, second -> first.plus(second) }
    }
}
