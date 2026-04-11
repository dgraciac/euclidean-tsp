package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge

/**
 * Optimizacion subgradiente para calcular candidatos mejorados basados en 1-tree.
 *
 * El algoritmo de Held-Karp usa relajacion lagrangiana sobre el 1-tree:
 * 1. Empezar con multiplicadores pi_i = 0 para cada ciudad
 * 2. Calcular costes modificados: c'(i,j) = c(i,j) - pi_i - pi_j
 * 3. Calcular el MST con costes modificados (1-tree)
 * 4. Para cada ciudad: si grado en 1-tree > 2, aumentar pi; si < 2, reducir pi
 * 5. Repetir. El coste del 1-tree + sum(2*pi) es una cota inferior del tour optimo
 * 6. Usar los costes modificados finales para α-nearness
 *
 * Los multiplicadores "empujan" el 1-tree hacia una estructura similar a un tour,
 * produciendo candidatos mucho mejores que el MST basico.
 *
 * @param points conjunto de puntos
 * @param k numero de candidatos por punto
 * @param iterations numero de iteraciones de subgradiente
 * @return mapa de punto -> lista de K candidatos ordenados por α-nearness mejorada
 *
 * Complejidad: O(iterations * n^2) — cada iteracion calcula MST O(n^2)
 * Con iterations constante (e.g., 50): O(n^2)
 */
fun buildSubgradientCandidates(
    points: Set<Point>,
    k: Int,
    iterations: Int = 50,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val n = pointList.size
    if (n <= 3) return pointList.associateWith { p -> pointList.filter { it != p } }

    // Precomputar distancias
    val dist = Array(n) { i -> DoubleArray(n) { j -> pointList[i].distance(pointList[j]) } }

    // Multiplicadores lagrangianos
    val pi = DoubleArray(n) { 0.0 }
    var stepSize = 1.0
    var bestLowerBound = Double.NEGATIVE_INFINITY

    // Iteraciones de subgradiente
    for (iter in 0 until iterations) {
        // Calcular costes modificados
        val modDist = Array(n) { i -> DoubleArray(n) { j -> dist[i][j] - pi[i] - pi[j] } }

        // Calcular MST con costes modificados usando Prim
        val mstEdges = computeMstPrim(modDist, n)

        // Calcular grado de cada nodo en el MST
        val degree = IntArray(n)
        var mstCost = 0.0
        for ((u, v) in mstEdges) {
            degree[u]++
            degree[v]++
            mstCost += modDist[u][v]
        }

        // Cota inferior: coste MST + sum(2 * pi_i)
        val lowerBound = mstCost + 2.0 * pi.sum()
        if (lowerBound > bestLowerBound) {
            bestLowerBound = lowerBound
        }

        // Subgradiente: d_i = 2 - degree_i (queremos grado 2 en cada nodo para un tour)
        val subgradient = IntArray(n) { 2 - degree[it] }
        val subgradientNorm = subgradient.sumOf { it * it }.toDouble()

        if (subgradientNorm == 0.0) break // Optimo encontrado (todos grado 2)

        // Actualizar multiplicadores
        for (i in 0 until n) {
            pi[i] += stepSize * subgradient[i]
        }

        // Reducir step size
        stepSize *= 0.99
    }

    // Calcular α-nearness con los multiplicadores finales
    val modDist = Array(n) { i -> DoubleArray(n) { j -> dist[i][j] - pi[i] - pi[j] } }
    val mstEdges = computeMstPrim(modDist, n)

    // Construir arbol de adyacencia del MST final
    val mstAdj = Array(n) { mutableListOf<Pair<Int, Double>>() }
    val inMst = HashSet<Long>()
    for ((u, v) in mstEdges) {
        mstAdj[u].add(Pair(v, modDist[u][v]))
        mstAdj[v].add(Pair(u, modDist[v][u]))
        inMst.add(u.toLong() * n + v)
        inMst.add(v.toLong() * n + u)
    }

    // Calcular max edge on path para α-nearness
    val maxEdgeToAll = Array(n) { root -> computeMaxEdgesFromRoot(root, mstAdj, n) }

    // Construir listas de candidatos basadas en α modificado
    val kActual = minOf(k, n - 1)
    return pointList
        .mapIndexed { i, u ->
            u to
                pointList
                    .mapIndexedNotNull { j, v ->
                        if (i == j) return@mapIndexedNotNull null
                        val modCost = modDist[i][j]
                        val maxEdge = maxEdgeToAll[i][j]
                        val alpha = modCost - maxEdge
                        Pair(v, alpha)
                    }.sortedBy { it.second }
                    .take(kActual)
                    .map { it.first }
        }.toMap()
}

/**
 * Calcula MST con algoritmo de Prim sobre matriz de distancias.
 * @return lista de aristas (u, v) del MST
 * Complejidad: O(n^2)
 */
private fun computeMstPrim(
    dist: Array<DoubleArray>,
    n: Int,
): List<Pair<Int, Int>> {
    val inMst = BooleanArray(n)
    val minEdge = DoubleArray(n) { Double.MAX_VALUE }
    val parent = IntArray(n) { -1 }
    minEdge[0] = 0.0
    val edges = mutableListOf<Pair<Int, Int>>()

    for (step in 0 until n) {
        var u = -1
        for (i in 0 until n) {
            if (!inMst[i] && (u == -1 || minEdge[i] < minEdge[u])) {
                u = i
            }
        }
        inMst[u] = true
        if (parent[u] != -1) {
            edges.add(Pair(parent[u], u))
        }
        for (v in 0 until n) {
            if (!inMst[v] && dist[u][v] < minEdge[v]) {
                minEdge[v] = dist[u][v]
                parent[v] = u
            }
        }
    }

    return edges
}

/**
 * Desde un nodo raiz, calcula la arista maxima en el camino MST a cada otro nodo.
 * Complejidad: O(n)
 */
private fun computeMaxEdgesFromRoot(
    root: Int,
    mstAdj: Array<MutableList<Pair<Int, Double>>>,
    n: Int,
): DoubleArray {
    val result = DoubleArray(n) { 0.0 }
    val visited = BooleanArray(n)
    visited[root] = true

    data class Entry(
        val node: Int,
        val maxSoFar: Double,
    )

    val stack = ArrayDeque<Entry>()
    stack.addLast(Entry(root, 0.0))

    while (stack.isNotEmpty()) {
        val (node, maxSoFar) = stack.removeLast()
        for ((neighbor, weight) in mstAdj[node]) {
            if (visited[neighbor]) continue
            visited[neighbor] = true
            val newMax = maxOf(maxSoFar, weight)
            result[neighbor] = newMax
            stack.addLast(Entry(neighbor, newMax))
        }
    }

    return result
}
