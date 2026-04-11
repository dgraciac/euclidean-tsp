package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * Nearest neighbor empezando desde un punto especifico.
 * Siempre visita el punto no visitado mas cercano.
 *
 * @param points todos los puntos de la instancia
 * @param start punto de inicio
 * @return tour cerrado (primero == ultimo)
 * Complejidad: O(n^2)
 */
fun nearestNeighborFrom(
    points: Set<Point>,
    start: Point,
): List<Point> {
    val remaining = points.toMutableSet()
    val tour = mutableListOf<Point>()
    tour.add(start)
    remaining.remove(start)

    while (remaining.isNotEmpty()) {
        val current = tour.last()
        val nearest = remaining.minBy { it.distance(current) }
        tour.add(nearest)
        remaining.remove(nearest)
    }

    tour.add(tour.first())
    return tour
}

/**
 * Farthest insertion: inicializa con la arista mas larga, luego inserta siempre
 * el punto mas lejano al tour actual en la posicion que minimiza el incremento de longitud.
 *
 * @param points todos los puntos de la instancia
 * @return tour cerrado (primero == ultimo)
 * Complejidad: O(n^2) — n iteraciones, cada una busca el punto mas lejano O(n) y la mejor posicion O(n)
 */
fun farthestInsertion(points: Set<Point>): List<Point> {
    val remaining = points.toMutableList()

    // Encontrar el par mas distante como semilla
    var maxDist = 0.0
    var p1 = remaining[0]
    var p2 = remaining[1]
    for (i in remaining.indices) {
        for (j in i + 1 until remaining.size) {
            val d = remaining[i].distance(remaining[j])
            if (d > maxDist) {
                maxDist = d
                p1 = remaining[i]
                p2 = remaining[j]
            }
        }
    }

    val tour = mutableListOf(p1, p2, p1) // Tour cerrado
    remaining.remove(p1)
    remaining.remove(p2)

    while (remaining.isNotEmpty()) {
        // Encontrar el punto mas lejano al tour
        var farthest = remaining[0]
        var farthestDist = 0.0
        for (point in remaining) {
            val minDistToTour = tour.dropLast(1).minOf { it.distance(point) }
            if (minDistToTour > farthestDist) {
                farthestDist = minDistToTour
                farthest = point
            }
        }

        // Insertar en la mejor posicion (minimiza incremento de longitud)
        var bestIdx = 1
        var bestCost = Double.POSITIVE_INFINITY
        for (i in 0 until tour.size - 1) {
            val cost = tour[i].distance(farthest) + farthest.distance(tour[i + 1]) - tour[i].distance(tour[i + 1])
            if (cost < bestCost) {
                bestCost = cost
                bestIdx = i + 1
            }
        }

        tour.add(bestIdx, farthest)
        remaining.remove(farthest)
    }

    return tour
}

/**
 * Convex hull + insercion por ratio (sin validacion).
 * Usa el convex hull como tour inicial e inserta puntos interiores.
 *
 * @param points todos los puntos de la instancia
 * @return tour cerrado (primero == ultimo)
 * Complejidad: O(n^2)
 */
fun convexHullInsertion(points: Set<Point>): List<Point> {
    val coordinates = points.map { it.toCoordinate() }.toTypedArray()
    val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
    val hullCoords = hull.coordinates.dropLast(1)

    val remaining = points.toMutableSet()
    val tour =
        hullCoords
            .map { coord -> remaining.first { it.x == coord.x && it.y == coord.y } }
            .toMutableList()
    tour.forEach { remaining.remove(it) }
    tour.add(tour.first())

    for (point in remaining) {
        var bestIdx = 1
        var bestRatio = Double.POSITIVE_INFINITY
        for (i in 0 until tour.size - 1) {
            val distAB = tour[i].distance(tour[i + 1])
            if (distAB == 0.0) continue
            val ratio = (tour[i].distance(point) + point.distance(tour[i + 1])) / distAB
            if (ratio < bestRatio) {
                bestRatio = ratio
                bestIdx = i + 1
            }
        }
        tour.add(bestIdx, point)
    }

    return tour
}

/**
 * Peeling de convex hulls + insercion por ratio.
 * Descompone en capas concentricas e inserta punto a punto.
 *
 * @param points todos los puntos de la instancia
 * @return tour cerrado (primero == ultimo)
 * Complejidad: O(n^2)
 */
fun peelingInsertion(points: Set<Point>): List<Point> {
    val layers = mutableListOf<List<Point>>()
    val remaining = points.toMutableSet()

    while (remaining.size >= 3) {
        val coordinates = remaining.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, GeometryFactory()).convexHull
        val hullCoordinates = hull.coordinates.dropLast(1)
        val hullPoints = hullCoordinates.map { coord -> remaining.first { it.x == coord.x && it.y == coord.y } }

        if (hullPoints.size < 3) {
            layers.add(remaining.toList())
            remaining.clear()
        } else {
            layers.add(hullPoints)
            remaining.removeAll(hullPoints.toSet())
        }
    }
    if (remaining.isNotEmpty()) layers.add(remaining.toList())

    val tour = layers.first().toMutableList()
    tour.add(tour.first())

    for (layerIndex in 1 until layers.size) {
        for (point in layers[layerIndex]) {
            var bestIdx = 1
            var bestRatio = Double.POSITIVE_INFINITY
            for (i in 0 until tour.size - 1) {
                val distAB = tour[i].distance(tour[i + 1])
                if (distAB == 0.0) continue
                val ratio = (tour[i].distance(point) + point.distance(tour[i + 1])) / distAB
                if (ratio < bestRatio) {
                    bestRatio = ratio
                    bestIdx = i + 1
                }
            }
            tour.add(bestIdx, point)
        }
    }

    return tour
}

/**
 * Greedy edge construction: ordena todas las aristas por longitud y las añade
 * al tour si no violan las restricciones (grado max 2, no cierran ciclo prematuro).
 *
 * @param points todos los puntos de la instancia
 * @return tour cerrado (primero == ultimo)
 * Complejidad: O(n^2 log n) — O(n^2) aristas, ordenar, iterar
 */
fun greedyConstruction(points: Set<Point>): List<Point> {
    val pointList = points.toList()
    val n = pointList.size

    // Generar todas las aristas con sus distancias
    data class Edge(
        val a: Int,
        val b: Int,
        val dist: Double,
    )

    val edges = mutableListOf<Edge>()
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            edges.add(Edge(i, j, pointList[i].distance(pointList[j])))
        }
    }
    edges.sortBy { it.dist }

    // Grado de cada nodo y union-find para detectar ciclos prematuros
    val degree = IntArray(n)
    val parent = IntArray(n) { it }
    var edgeCount = 0

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

    fun union(
        x: Int,
        y: Int,
    ) {
        parent[find(x)] = find(y)
    }

    // Adjacencia para reconstruir el tour
    val adj = Array(n) { mutableListOf<Int>() }

    for (edge in edges) {
        if (edgeCount == n) break
        if (degree[edge.a] >= 2 || degree[edge.b] >= 2) continue
        // No cerrar ciclo prematuro (solo al final)
        if (edgeCount < n - 1 && find(edge.a) == find(edge.b)) continue

        adj[edge.a].add(edge.b)
        adj[edge.b].add(edge.a)
        degree[edge.a]++
        degree[edge.b]++
        union(edge.a, edge.b)
        edgeCount++
    }

    // Reconstruir tour desde la lista de adyacencia
    val tour = mutableListOf<Point>()
    val visited = BooleanArray(n)
    var current = 0
    for (step in 0 until n) {
        tour.add(pointList[current])
        visited[current] = true
        val next = adj[current].firstOrNull { !visited[it] }
        if (next != null) {
            current = next
        }
    }
    tour.add(tour.first())
    return tour
}
