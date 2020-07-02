package com.github.dgraciac.euclideantsp.christofides

import java.util.*


/**
 *
 *
 * @author      Bjørn Harald Olsen
 * @author      Oscar
 * @version     1.0
 */
class GraphNode(var name: Int) {
    var childList: ArrayList<GraphNode> = ArrayList()
    var isVisited: Boolean = false

    fun setVisited() {
        isVisited = true
    }

    val numberOfChilds: Int
        get() = childList.size

    fun setNotVisited() {
        isVisited = false
    }

    fun addChild(node: GraphNode) {
        if (name != node.name) {
            childList.add(node)
        }
    }

    fun removeChild(node: GraphNode?) {
        childList.remove(node)
    }

    fun hasMoreChilds(): Boolean {
        return childList.size > 0
    }

    fun getNextChild(goal: Int, path: Vector<Int>, firstTime: Boolean) {
        //om vi nått vårt mål så avsluta
        if (name == goal && !firstTime) {
            path.add(name)
        } else {
            //om fler vägar från denna nod, lägg till noden och fortsätt längs den första bästa av kanterna, plocka dessutom bort kanten.
            if (childList.size > 0) {
                val tmpNode = childList.removeAt(0) as GraphNode
                tmpNode.removeChild(this) //ta bort kanten från andra hållet
                path.add(name)
                tmpNode.getNextChild(goal, path, false)
            }
        }
    }
}