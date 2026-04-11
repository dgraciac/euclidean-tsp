package com.github.dgraciac.euclideantsp

/**
 * Genera todas las permutaciones de los elementos de la lista.
 * Implementacion recursiva: fija cada elemento y permuta el resto.
 * Complejidad: O(n! * n) — genera n! permutaciones, cada una de tamaño n
 */
fun <E> List<E>.permute(): List<List<E>> {
    require(this.isNotEmpty())
    return when (this.size) {
        1 -> {
            listOf(this)
        }

        else -> {
            this
                .map { element: E ->
                    this.minusElement(element).permute().map { subPermutation: List<E> ->
                        listOf(element).plus(subPermutation)
                    }
                }.reduce { first, second -> first.plus(second) }
        }
    }
}
