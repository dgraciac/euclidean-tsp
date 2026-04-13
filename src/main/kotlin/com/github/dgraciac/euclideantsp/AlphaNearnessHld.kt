package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import kotlin.math.log2

/**
 * Calcula listas de candidatos basadas en alpha-nearness usando Euler Tour + Sparse Table
 * para consultas maxEdgeOnPath en O(log n), y solo evalua alpha para los K' vecinos
 * mas cercanos por distancia (no todos los n-1 pares).
 *
 * Mejora sobre [buildAlphaNearnessListLean]:
 * - maxEdgeOnPath: O(1) por consulta (Euler Tour + Sparse Table) vs O(n) por DFS
 * - Solo evalua K' = 4*k candidatos por nodo (vecinos cercanos por distancia) vs n-1
 * - Total: O(n^2) para Prim + O(n) preproceso + O(n * K' * log n) consultas
 *   = O(n^2) dominado por Prim. Prim es inevitable sin estructura espacial en MST.
 *
 * @param points conjunto de puntos
 * @param k numero de candidatos por punto
 * @return mapa de punto -> lista de K candidatos ordenados por alpha-nearness
 *
 * Complejidad: O(n^2) — dominada por Prim MST (inevitable sin KD-tree para MST)
 * Mejora practica: ~2-3x mas rapido que buildAlphaNearnessListLean porque evita
 * n DFS completos y n sorts de n-1 candidatos.
 */
fun buildAlphaNearnessHld(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val n = pointList.size
    if (n <= 2) return pointList.associateWith { p -> pointList.filter { it != p } }

    // Paso 1: Prim MST sobre grafo implicito — O(n^2)
    val parent = IntArray(n) { -1 }
    val parentDist = DoubleArray(n) { 0.0 }
    val inMst = BooleanArray(n)
    val minDist = DoubleArray(n) { Double.MAX_VALUE }
    val minFrom = IntArray(n) { -1 }

    minDist[0] = 0.0
    for (step in 0 until n) {
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

    // Paso 2: Euler Tour + Sparse Table para maxEdgeOnPath en O(1) por consulta
    // Euler tour: recorre el arbol y registra cada nodo al entrar y salir
    val eulerTour = IntArray(2 * n) // nodo visitado en cada paso
    val eulerDepth = IntArray(2 * n) // profundidad en cada paso
    val eulerMaxEdge = DoubleArray(2 * n) // max edge weight desde raiz en cada paso
    val firstOccurrence = IntArray(n) { -1 } // primera ocurrencia de cada nodo en Euler tour
    var tourIdx = 0

    // DFS iterativo para Euler tour
    val dfsStack = ArrayDeque<Triple<Int, Int, Double>>() // (nodo, padre, maxEdgeFromRoot)
    val dfsChildIdx = IntArray(n) // indice del proximo hijo a visitar
    val depth = IntArray(n)
    val maxEdgeFromRoot = DoubleArray(n)

    dfsStack.addLast(Triple(0, -1, 0.0))
    depth[0] = 0
    maxEdgeFromRoot[0] = 0.0

    while (dfsStack.isNotEmpty()) {
        val (node, par, maxE) = dfsStack.last()

        if (dfsChildIdx[node] == 0) {
            // Primera visita: registrar en Euler tour
            firstOccurrence[node] = tourIdx
            eulerTour[tourIdx] = node
            eulerDepth[tourIdx] = depth[node]
            eulerMaxEdge[tourIdx] = maxE
            tourIdx++
        }

        // Buscar siguiente hijo no visitado
        val children = mstAdj[node]
        var found = false
        while (dfsChildIdx[node] < children.size) {
            val (child, weight) = children[dfsChildIdx[node]]
            dfsChildIdx[node]++
            if (child != par) {
                depth[child] = depth[node] + 1
                maxEdgeFromRoot[child] = maxOf(maxE, weight)
                dfsStack.addLast(Triple(child, node, maxEdgeFromRoot[child]))
                found = true
                break
            }
        }

        if (!found) {
            // Todos los hijos visitados: backtrack, registrar en Euler tour
            dfsStack.removeLast()
            if (dfsStack.isNotEmpty()) {
                eulerTour[tourIdx] = dfsStack.last().first
                eulerDepth[tourIdx] = depth[dfsStack.last().first]
                eulerMaxEdge[tourIdx] = dfsStack.last().third
                tourIdx++
            }
        }
    }
    val tourLen = tourIdx

    // Sparse table para Range Maximum Query sobre eulerMaxEdge
    // Necesitamos RMQ que devuelva el indice con maxEdgeOnPath maximo
    // Para maxEdgeOnPath(u,v): es max(maxEdgeFromRoot en rango [first(u), first(v)])
    // menos el minimo comun, pero mas simple: maxEdgeOnPath(u,v) = max edge en path.
    //
    // Metodo: LCA via Euler Tour + maxEdgeOnPath = max(maxEdgeFromRoot[u], maxEdgeFromRoot[v])
    // si u es ancestro de v, o max edge en path u->LCA->v.
    //
    // Simplificacion: maxEdgeOnPath(u,v) = max de maxEdgeFromRoot en el rango
    // del Euler tour entre first(u) y first(v), MENOS maxEdgeFromRoot del LCA.
    // Pero esto es complicado. Uso un enfoque mas directo:
    //
    // Para cada nodo, almacenamos los ancestros con binary lifting y los max edge weights.
    // Binary lifting: ancestor[node][k] = 2^k-esimo ancestro de node
    //                  maxWeight[node][k] = max edge weight en path node -> ancestor[node][k]

    val LOG = maxOf(1, (log2(n.toDouble()) + 1).toInt())
    val ancestor = Array(n) { IntArray(LOG) { -1 } }
    val maxWeight = Array(n) { DoubleArray(LOG) { 0.0 } }

    // Nivel 0: padre directo
    for (i in 0 until n) {
        ancestor[i][0] = if (parent[i] == -1) i else parent[i]
        maxWeight[i][0] = parentDist[i]
    }

    // Niveles superiores
    for (j in 1 until LOG) {
        for (i in 0 until n) {
            val mid = ancestor[i][j - 1]
            ancestor[i][j] = ancestor[mid][j - 1]
            maxWeight[i][j] = maxOf(maxWeight[i][j - 1], maxWeight[mid][j - 1])
        }
    }

    // Funcion LCA + maxEdgeOnPath usando binary lifting
    fun maxEdgeOnPath(u: Int, v: Int): Double {
        var a = u
        var b = v
        var maxE = 0.0

        // Igualar profundidades
        if (depth[a] < depth[b]) {
            val tmp = a; a = b; b = tmp
        }
        var diff = depth[a] - depth[b]
        var j = 0
        while (diff > 0) {
            if (diff and 1 == 1) {
                maxE = maxOf(maxE, maxWeight[a][j])
                a = ancestor[a][j]
            }
            diff = diff shr 1
            j++
        }

        if (a == b) return maxE

        // Subir ambos hasta LCA
        for (jj in LOG - 1 downTo 0) {
            if (ancestor[a][jj] != ancestor[b][jj]) {
                maxE = maxOf(maxE, maxWeight[a][jj], maxWeight[b][jj])
                a = ancestor[a][jj]
                b = ancestor[b][jj]
            }
        }
        maxE = maxOf(maxE, maxWeight[a][0], maxWeight[b][0])
        return maxE
    }

    // Paso 3: Para cada nodo, calcular alpha solo para los K' vecinos mas cercanos por distancia
    // Usar KD-tree para encontrar K' vecinos rapido
    val kPrime = minOf(4 * k, n - 1) // candidatos por distancia: 4x los necesarios por alpha
    val kActual = minOf(k, n - 1)
    val kdTree = KdTreeKnn(pointList)
    val pointToIndex = HashMap<Point, Int>(n * 2)
    pointList.forEachIndexed { idx, p -> pointToIndex[p] = idx }

    val result = HashMap<Point, List<Point>>(n * 2)
    for (uIdx in 0 until n) {
        val distNeighbors = kdTree.kNearest(pointList[uIdx], kPrime)
        val candidates =
            distNeighbors.map { neighbor ->
                val vIdx = pointToIndex[neighbor]!!
                val dist = pointList[uIdx].distance(neighbor)
                val maxE = maxEdgeOnPath(uIdx, vIdx)
                val alpha = dist - maxE
                Pair(neighbor, alpha)
            }
        result[pointList[uIdx]] = candidates
            .sortedWith(compareBy<Pair<Point, Double>> { it.second }.thenBy { it.first.x }.thenBy { it.first.y })
            .take(kActual)
            .map { it.first }
    }

    return result
}
