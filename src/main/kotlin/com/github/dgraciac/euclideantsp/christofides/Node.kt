package com.github.dgraciac.euclideantsp.christofides

import java.util.*


/**
 *
 *
 * @author      Bjørn Harald Olsen
 * @author      Oscar Täckström
 * @version     1.0
 */
class Node {
    var isRoot: Boolean
    var number: Int
    var children: ArrayList<Node>?

    constructor(_n: Int) {
        number = _n
        children = null
        isRoot = false
    }

    constructor(_n: Int, _isRoot: Boolean) {
        number = _n
        children = null
        isRoot = _isRoot
    }

    fun addChild(_node: Node?) {
        if (children == null) children = ArrayList()
        children!!.add(_node!!)
    }

    fun visitBuildRoute(_route: ArrayList<Int>) {
        _route.add(number)
        if (children == null) return
        for (i in children!!.indices) {
            children!![i].visitBuildRoute(_route)
        }
    }

    fun visitFindOddDegreeNodes(_oddNodes: ArrayList<Int>) {
        if (children == null) {
            _oddNodes.add(number)
            return
        }
        if (isRoot && children!!.size % 2 != 0) _oddNodes.add(number)
        if (!isRoot && children!!.size % 2 == 0) _oddNodes.add(number)
        for (i in children!!.indices) {
            children!![i].visitFindOddDegreeNodes(_oddNodes)
        }
    }

}