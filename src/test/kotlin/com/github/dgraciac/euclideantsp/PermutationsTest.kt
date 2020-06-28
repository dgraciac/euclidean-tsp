package com.github.dgraciac.euclideantsp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PermutationsTest {

    @ParameterizedTest
    @MethodSource("fixtures")
    fun permutes(list: List<Any>, permutations: List<List<Any>>) {
        assertThat(list.permute()).containsExactlyInAnyOrder(*permutations.toTypedArray())
    }

    private companion object {
        @JvmStatic
        fun fixtures(): List<Arguments> =
            listOf(
                Arguments.of(
                    listOf(1, 2),
                    listOf(
                        listOf(1, 2),
                        listOf(2, 1)
                    )
                ),
                Arguments.of(
                    listOf(1, 2, 3),
                    listOf(
                        listOf(1, 2, 3),
                        listOf(1, 3, 2),
                        listOf(2, 1, 3),
                        listOf(2, 3, 1),
                        listOf(3, 1, 2),
                        listOf(3, 2, 1)
                    )
                )
            )
    }
}

private fun <E> List<E>.permute(): List<List<E>> {
    return when (this.size) {
        0 -> emptyList()
        1 -> listOf(this)
        else -> {
            val map: List<List<List<E>>> = this.map { element: E ->
                val minusElement = this.minusElement(element)
                val subPermutations = minusElement.permute()
                val map: List<List<E>> = subPermutations.map { subPermutation: List<E> ->
                    val plus = listOf(element).plus(subPermutation)
                    plus
                }
                map
            }.
            map
        }
        }
    }
}
