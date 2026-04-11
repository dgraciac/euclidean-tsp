package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import org.jgrapht.Graph
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge

/**
 * Calcula listas de candidatos basadas en α-nearness (1-tree).
 *
 * α-nearness es la metrica usada por LKH para seleccionar candidatos.
 * Para cada arista (u,v), α(u,v) mide cuanto empeoraria el 1-tree si
 * se forzara a incluir esa arista. Aristas con α bajo son las mas probables
 * de estar en el tour optimo.
 *
 * Algoritmo:
 * 1. Construir grafo completo ponderado — O(n^2)
 * 2. Calcular MST (arbol generador minimo) — O(n^2) con Prim
 * 3. Para cada par de puntos (u,v):
 *    - Si (u,v) esta en el MST: α = 0
 *    - Si no: α = d(u,v) - maxEdgeOnPath(u,v,MST)
 *      (cuanto añade la arista sobre la arista mas larga del camino en el MST)
 * 4. Para cada punto, ordenar vecinos por α y tomar los K mejores
 *
 * @param points conjunto de puntos
 * @param k numero de candidatos por punto
 * @return mapa de punto -> lista de K candidatos ordenados por α-nearness
 *
 * Complejidad: O(n^2 log n) — dominada por la construccion del grafo y MST
 */
fun buildAlphaNearnessList(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val n = pointList.size
    if (n <= 2) return pointList.associateWith { p -> pointList.filter { it != p } }

    // Paso 1-2: Construir grafo completo y calcular MST
    val graph =
        DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
    pointList.forEach { graph.addVertex(it) }
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            val edge = graph.addEdge(pointList[i], pointList[j])
            graph.setEdgeWeight(edge, pointList[i].distance(pointList[j]))
        }
    }

    val mst = PrimMinimumSpanningTree(graph).spanningTree

    // Construir arbol de adyacencia del MST para buscar caminos
    val mstAdj = HashMap<Point, MutableList<Pair<Point, Double>>>()
    pointList.forEach { mstAdj[it] = mutableListOf() }
    for (edge in mst.edges) {
        val u = graph.getEdgeSource(edge)
        val v = graph.getEdgeTarget(edge)
        val w = graph.getEdgeWeight(edge)
        mstAdj[u]!!.add(Pair(v, w))
        mstAdj[v]!!.add(Pair(u, w))
    }

    // Paso 3: Calcular maxEdgeOnPath para cada par usando BFS/DFS desde cada punto
    // Para cada punto u, calcular la arista maxima en el camino a todos los demas en el MST
    val maxEdgeToAll = HashMap<Point, Map<Point, Double>>()
    for (u in pointList) {
        maxEdgeToAll[u] = computeMaxEdgesFromNode(u, mstAdj, n)
    }

    // Paso 4: Calcular α para cada par y construir listas de candidatos
    val kActual = minOf(k, n - 1)
    return pointList.associateWith { u ->
        pointList
            .filter { it != u }
            .map { v ->
                val dist = u.distance(v)
                val maxEdge = maxEdgeToAll[u]?.get(v) ?: 0.0
                val alpha = dist - maxEdge // Si esta en MST, maxEdge = dist, alpha = 0
                Pair(v, alpha)
            }.sortedBy { it.second }
            .take(kActual)
            .map { it.first }
    }
}

/**
 * Desde un nodo raiz, calcula la arista de peso maximo en el camino del MST
 * a cada otro nodo. Usa DFS.
 *
 * @return mapa de nodo -> peso maximo de arista en el camino raiz->nodo en el MST
 * Complejidad: O(n)
 */
private fun computeMaxEdgesFromNode(
    root: Point,
    mstAdj: Map<Point, List<Pair<Point, Double>>>,
    n: Int,
): Map<Point, Double> {
    val result = HashMap<Point, Double>(n * 2)
    val visited = HashSet<Point>(n * 2)
    result[root] = 0.0
    visited.add(root)

    // DFS iterativo
    data class StackEntry(
        val node: Point,
        val maxSoFar: Double,
    )

    val stack = ArrayDeque<StackEntry>()
    stack.addLast(StackEntry(root, 0.0))

    while (stack.isNotEmpty()) {
        val (node, maxSoFar) = stack.removeLast()
        for ((neighbor, weight) in mstAdj[node] ?: emptyList()) {
            if (neighbor in visited) continue
            visited.add(neighbor)
            val newMax = maxOf(maxSoFar, weight)
            result[neighbor] = newMax
            stack.addLast(StackEntry(neighbor, newMax))
        }
    }

    return result
}
