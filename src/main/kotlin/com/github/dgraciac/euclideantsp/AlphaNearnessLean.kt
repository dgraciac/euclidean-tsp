package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Calcula listas de candidatos basadas en alpha-nearness sin construir grafo completo.
 *
 * Misma semantica que [buildAlphaNearnessList] pero con implementacion "lean":
 * - MST con Prim sobre grafo implicito (sin JGraphT, sin objetos Edge)
 * - Memoria adicional: O(n) arrays en lugar de O(n^2) objetos Java
 * - Para n=11,849: ~100KB en lugar de ~7GB
 *
 * Algoritmo:
 * 1. Prim MST sobre grafo implicito — O(n^2) tiempo, O(n) memoria
 * 2. DFS desde cada nodo para maxEdgeOnPath — O(n^2) total
 * 3. Para cada nodo, calcular alpha y tomar K mejores — O(n^2 log K) con partial sort
 *
 * @param points conjunto de puntos
 * @param k numero de candidatos por punto
 * @return mapa de punto -> lista de K candidatos ordenados por alpha-nearness
 *
 * Complejidad: O(n^2) tiempo (dominado por Prim y DFS), O(n) memoria adicional
 */
fun buildAlphaNearnessListLean(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val n = pointList.size
    if (n <= 2) return pointList.associateWith { p -> pointList.filter { it != p } }

    // Paso 1: Prim MST sobre grafo implicito
    // parent[i] = indice del padre de i en el MST (-1 para raiz)
    // parentDist[i] = peso de la arista i-parent[i]
    val parent = IntArray(n) { -1 }
    val parentDist = DoubleArray(n) { 0.0 }
    val inMst = BooleanArray(n)
    val minDist = DoubleArray(n) { Double.MAX_VALUE }
    val minFrom = IntArray(n) { -1 }

    // Empezar desde nodo 0
    minDist[0] = 0.0
    for (step in 0 until n) {
        // Encontrar nodo no-MST con minima distancia al MST
        var u = -1
        var best = Double.MAX_VALUE
        for (i in 0 until n) {
            if (!inMst[i] && minDist[i] < best) {
                best = minDist[i]
                u = i
            }
        }
        if (u == -1) break
        inMst[u] = true
        parent[u] = minFrom[u]
        parentDist[u] = best

        // Actualizar distancias de vecinos no-MST
        for (v in 0 until n) {
            if (!inMst[v]) {
                val dist = pointList[u].distance(pointList[v])
                if (dist < minDist[v]) {
                    minDist[v] = dist
                    minFrom[v] = u
                }
            }
        }
    }

    // Construir adyacencia del MST
    val mstAdj = Array(n) { mutableListOf<Pair<Int, Double>>() }
    for (i in 1 until n) {
        val p = parent[i]
        val w = parentDist[i]
        mstAdj[i].add(Pair(p, w))
        mstAdj[p].add(Pair(i, w))
    }

    // Paso 2: Para cada nodo, DFS para maxEdgeOnPath a todos los demas
    // maxEdge[u][v] = max edge weight on path u->v in MST
    // Solo almacenamos los K mejores candidatos por alpha, no la matriz completa
    val kActual = minOf(k, n - 1)
    val result = HashMap<Point, List<Point>>(n * 2)

    for (uIdx in 0 until n) {
        // DFS desde uIdx: calcular maxEdgeOnPath a todos los demas
        val maxEdge = DoubleArray(n) { -1.0 }
        maxEdge[uIdx] = 0.0
        val visited = BooleanArray(n)
        visited[uIdx] = true

        val stack = ArrayDeque<Pair<Int, Double>>()
        stack.addLast(Pair(uIdx, 0.0))

        while (stack.isNotEmpty()) {
            val (node, maxSoFar) = stack.removeLast()
            for ((neighbor, weight) in mstAdj[node]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true
                    val newMax = maxOf(maxSoFar, weight)
                    maxEdge[neighbor] = newMax
                    stack.addLast(Pair(neighbor, newMax))
                }
            }
        }

        // Calcular alpha para cada par y tomar K mejores
        data class Candidate(
            val index: Int,
            val alpha: Double,
        )

        val candidates = ArrayList<Candidate>(n - 1)
        for (vIdx in 0 until n) {
            if (vIdx == uIdx) continue
            val dist = pointList[uIdx].distance(pointList[vIdx])
            val alpha = dist - maxEdge[vIdx]
            candidates.add(Candidate(vIdx, alpha))
        }
        candidates.sortBy { it.alpha }

        result[pointList[uIdx]] = candidates.take(kActual).map { pointList[it.index] }
    }

    return result
}
