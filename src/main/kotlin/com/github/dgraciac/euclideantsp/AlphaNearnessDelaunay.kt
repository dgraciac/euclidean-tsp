package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder

/**
 * Calcula listas de candidatos basadas en alpha-nearness usando MST euclideo
 * via triangulacion de Delaunay (O(n log n)) en vez de Prim (O(n^2)).
 *
 * El MST euclideo es un subgrafo de la triangulacion de Delaunay (teorema clasico
 * de geometria computacional). Esto permite calcular el MST en O(n log n):
 * 1. Delaunay con JTS — O(n log n)
 * 2. Kruskal sobre aristas Delaunay — O(n log n) (Delaunay tiene O(n) aristas)
 * 3. DFS desde cada nodo para maxEdgeOnPath — O(n^2)
 * 4. Alpha y K mejores por nodo — O(n^2)
 *
 * La mejora sobre [buildAlphaNearnessListLean] es eliminar el Prim O(n^2) que
 * era el cuello de botella. Los pasos 3-4 siguen siendo O(n^2) pero con constante
 * pequena (verificado en E062: no son el cuello de botella).
 *
 * @param points conjunto de puntos
 * @param k numero de candidatos por punto
 * @return mapa de punto -> lista de K candidatos ordenados por alpha-nearness
 *
 * Complejidad: O(n^2) total (pasos 3-4 dominan), pero con Prim eliminado el
 * factor constante es significativamente menor para n grande.
 */
fun buildAlphaNearnessDelaunay(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val n = pointList.size
    if (n <= 2) return pointList.associateWith { p -> pointList.filter { it != p } }

    // Mapa de coordenadas a indice para busqueda rapida
    val pointToIndex = HashMap<Long, MutableList<Int>>(n * 2)
    for (i in 0 until n) {
        val key = coordKey(pointList[i].x, pointList[i].y)
        pointToIndex.getOrPut(key) { mutableListOf() }.add(i)
    }

    // Paso 1: Triangulacion de Delaunay — O(n log n)
    val factory = GeometryFactory()
    val builder = DelaunayTriangulationBuilder()
    builder.setSites(
        factory.createMultiPointFromCoords(
            pointList.map { it.toCoordinate() }.toTypedArray(),
        ),
    )
    val triangles = builder.getTriangles(factory)

    // Paso 2: Extraer aristas Delaunay y ejecutar Kruskal — O(n log n)
    data class Edge(
        val u: Int,
        val v: Int,
        val weight: Double,
    )

    val edges = mutableListOf<Edge>()
    val seenEdges = HashSet<Long>()

    for (i in 0 until triangles.numGeometries) {
        val triangle = triangles.getGeometryN(i)
        val coords = triangle.coordinates
        for (a in 0 until 3) {
            for (b in a + 1 until 3) {
                val idxA = findIndex(pointToIndex, pointList, coords[a].x, coords[a].y)
                val idxB = findIndex(pointToIndex, pointList, coords[b].x, coords[b].y)
                if (idxA == -1 || idxB == -1) continue
                val edgeKey = if (idxA < idxB) idxA.toLong() * n + idxB else idxB.toLong() * n + idxA
                if (seenEdges.add(edgeKey)) {
                    edges.add(Edge(idxA, idxB, pointList[idxA].distance(pointList[idxB])))
                }
            }
        }
    }

    edges.sortBy { it.weight }

    // Kruskal con union-find
    val parent = IntArray(n) { it }
    val rank = IntArray(n)
    val mstParent = IntArray(n) { -1 }
    val mstParentDist = DoubleArray(n)

    fun find(x: Int): Int {
        var r = x
        while (parent[r] != r) r = parent[r]
        var c = x
        while (c != r) {
            val next = parent[c]
            parent[c] = r
            c = next
        }
        return r
    }

    var mstEdges = 0
    val mstAdj = Array(n) { mutableListOf<Pair<Int, Double>>() }

    for (edge in edges) {
        if (mstEdges == n - 1) break
        val ru = find(edge.u)
        val rv = find(edge.v)
        if (ru == rv) continue

        // Union by rank
        if (rank[ru] < rank[rv]) {
            parent[ru] = rv
        } else if (rank[ru] > rank[rv]) {
            parent[rv] = ru
        } else {
            parent[rv] = ru
            rank[ru]++
        }

        mstAdj[edge.u].add(Pair(edge.v, edge.weight))
        mstAdj[edge.v].add(Pair(edge.u, edge.weight))
        mstEdges++
    }

    // Paso 3: Para cada nodo, DFS para maxEdgeOnPath a todos los demas — O(n^2)
    val kActual = minOf(k, n - 1)
    val result = HashMap<Point, List<Point>>(n * 2)

    for (uIdx in 0 until n) {
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

        // Paso 4: Calcular alpha para cada par y tomar K mejores
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
        candidates.sortWith(compareBy<Candidate> { it.alpha }.thenBy { pointList[it.index].x }.thenBy { pointList[it.index].y })

        result[pointList[uIdx]] = candidates.take(kActual).map { pointList[it.index] }
    }

    return result
}

/** Clave hash para coordenadas. */
private fun coordKey(
    x: Double,
    y: Double,
): Long = java.lang.Double.doubleToLongBits(x) xor (java.lang.Double.doubleToLongBits(y) * 31)

/** Busca el indice de un punto por coordenadas. */
private fun findIndex(
    index: Map<Long, List<Int>>,
    pointList: List<Point>,
    x: Double,
    y: Double,
): Int {
    val key = coordKey(x, y)
    val candidates = index[key] ?: return pointList.indexOfFirst { it.x == x && it.y == y }
    for (idx in candidates) {
        if (pointList[idx].x == x && pointList[idx].y == y) return idx
    }
    return pointList.indexOfFirst { it.x == x && it.y == y }
}
