package com.github.dgraciac.euclideantsp.christofides

/**
 *
 *
 * @author      Bjørn Harald Olsen
 * @author      Oscar Täckström
 * @version     1.0
 */
class Edge(var from: Int, var to: Int, var cost: Double) : Comparable<Any?> {
    override fun compareTo(other: Any?): Int {
        val e = other as Edge
        return if (cost == e.cost) 0 else if (cost > e.cost) 1 else -1
    }
}