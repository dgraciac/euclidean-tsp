package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder

/**
 * SolverN1 — Insercion geometrica desde convex hull guiada por Delaunay
 *
 * Linea de investigacion: N (insercion geometrica)
 * Experimento: E055
 *
 * Hipotesis: Usando la triangulacion de Delaunay como "mapa de vecindad natural",
 * podemos determinar geometricamente donde insertar cada punto interior sin
 * necesidad de probar todas las aristas del tour (estrategia combinatoria).
 * Para cada punto P, sus vecinos Delaunay que ya estan en el tour indican
 * directamente la posicion de insercion.
 *
 * Algoritmo:
 * 1. Triangulacion de Delaunay de todos los puntos — O(n log n)
 * 2. Convex hull como tour inicial — O(n log n)
 * 3. Ordenar puntos interiores por distancia perpendicular al tour (mas cercanos primero)
 * 4. Para cada punto P (en ese orden):
 *    a. Buscar entre los vecinos Delaunay de P los que estan en el tour
 *    b. Entre esos vecinos-en-tour, buscar un par (A,B) adyacente en el tour
 *    c. Si existe: insertar P entre A y B (posicion determinada geometricamente)
 *    d. Si no: fallback a insercion en la arista mas cercana (distancia punto-segmento)
 * 5. Busqueda local NL: 2-opt-nl + or-opt-nl + LK(2) + DB-nl + LK(2)
 *
 * Complejidad e2e: O(n^2)
 * - Paso 1: O(n log n)
 * - Paso 2: O(n log n)
 * - Paso 3: O(n * t) donde t es el tamaño del tour en cada iteracion ~ O(n^2) total
 * - Paso 4: O(n * d) donde d es el grado medio Delaunay (~6) ~ O(n) total
 *   (con fallback O(n) por punto, pero infrecuente)
 * - Paso 5: O(n^2) con NL
 *
 * Complejidad peor caso: O(n^2)
 */
class SolverN1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val points = instance.points
        val n = points.size

        // Paso 1: Triangulacion de Delaunay
        val delaunayAdj = buildDelaunayAdjacency(points)

        // Paso 2: Convex hull como tour inicial
        val factory = GeometryFactory()
        val coordinates = points.map { it.toCoordinate() }.toTypedArray()
        val hull = ConvexHull(coordinates, factory).convexHull
        val hullCoords = hull.coordinates.dropLast(1)
        val hullPoints =
            hullCoords.map { coord ->
                points.first { it.x == coord.x && it.y == coord.y }
            }

        val tour = hullPoints.toMutableList()
        val inTour = HashSet<Point>(n * 2)
        inTour.addAll(hullPoints)

        // Puntos interiores (no en hull)
        val interior = points.filter { it !in inTour }

        // Paso 3: Ordenar interiores por distancia minima al tour actual
        // (mas cercanos primero — se insertan con mas confianza geometrica)
        val sortedInterior =
            interior.sortedBy { p ->
                minDistanceToTour(p, tour)
            }

        // Paso 4: Insertar cada punto usando vecindad Delaunay
        // Mantenemos un mapa de posiciones para busqueda rapida
        val positionMap = HashMap<Point, Int>(n * 2)
        tour.forEachIndexed { idx, p -> positionMap[p] = idx }

        for (p in sortedInterior) {
            val insertIdx = findGeometricInsertionPoint(p, tour, delaunayAdj, positionMap)
            tour.add(insertIdx, p)
            // Actualizar positionMap: indices desde insertIdx en adelante se desplazan +1
            for (i in insertIdx until tour.size) {
                positionMap[tour[i]] = i
            }
            inTour.add(p)
        }

        tour.add(tour.first()) // Cerrar tour

        // Paso 5: Busqueda local NL
        val neighborLists = buildNeighborLists(points, k = 15)
        val afterTwoOpt = twoOptWithNeighborLists(tour, neighborLists)
        val afterOrOpt = orOptWithNeighborLists(afterTwoOpt, neighborLists)
        val afterTwoOpt2 = twoOptWithNeighborLists(afterOrOpt, neighborLists)
        val afterLk = linKernighan(afterTwoOpt2, neighborLists)
        val afterDb = doubleBridgePerturbationNl(afterLk, neighborLists, maxAttempts = 20)
        val finalTour = linKernighan(afterDb, neighborLists)

        return Tour(points = finalTour)
    }

    /**
     * Encuentra la posicion de insercion para P usando propiedades geometricas.
     *
     * Estrategia (en orden de prioridad):
     * 1. Buscar par de vecinos Delaunay de P que sean adyacentes en el tour → insertar entre ellos
     * 2. Si no hay par adyacente, buscar el vecino Delaunay en tour y la arista adyacente
     *    a ese vecino que minimiza el angulo de detour
     * 3. Fallback: arista del tour mas cercana por distancia punto-segmento
     *
     * @return indice en la lista tour donde insertar P (el punto se inserta ANTES de este indice)
     * Complejidad: O(d + t_fallback) donde d = grado Delaunay (~6), t_fallback = O(n) infrecuente
     */
    private fun findGeometricInsertionPoint(
        p: Point,
        tour: MutableList<Point>,
        delaunayAdj: Map<Point, Set<Point>>,
        positionMap: Map<Point, Int>,
    ): Int {
        val tourSize = tour.size
        val neighbors = delaunayAdj[p] ?: emptySet()

        // Vecinos Delaunay que ya estan en el tour
        val neighborsInTour = neighbors.filter { positionMap.containsKey(it) }

        if (neighborsInTour.size >= 2) {
            // Estrategia 1: buscar par de vecinos adyacentes en el tour
            for (a in neighborsInTour) {
                val posA = positionMap[a]!!
                for (b in neighborsInTour) {
                    if (a === b) continue
                    val posB = positionMap[b]!!
                    // Verificar si A y B son adyacentes en el tour
                    if (posB == (posA + 1) % tourSize) {
                        // Insertar entre A y B
                        return posB
                    }
                    if (posA == (posB + 1) % tourSize) {
                        // Insertar entre B y A
                        return posA
                    }
                }
            }

            // Estrategia 2: entre los vecinos Delaunay en tour, elegir el mas cercano
            // e insertar en la arista adyacente que produce menor detour
            val closestNeighbor = neighborsInTour.minBy { it.distance(p) }
            return findBestAdjacentEdge(p, closestNeighbor, tour, positionMap)
        } else if (neighborsInTour.size == 1) {
            // Solo un vecino Delaunay en tour: insertar en su mejor arista adyacente
            return findBestAdjacentEdge(p, neighborsInTour.first(), tour, positionMap)
        }

        // Estrategia 3 (fallback): arista del tour mas cercana por distancia punto-segmento
        return findNearestEdge(p, tour)
    }

    /**
     * Dado un punto P y un nodo del tour N, elige entre las dos aristas
     * adyacentes a N (N-prev y N-next) la que produce menor detour.
     *
     * Complejidad: O(1)
     */
    private fun findBestAdjacentEdge(
        p: Point,
        neighbor: Point,
        tour: List<Point>,
        positionMap: Map<Point, Int>,
    ): Int {
        val pos = positionMap[neighbor]!!
        val tourSize = tour.size
        val prevIdx = (pos - 1 + tourSize) % tourSize
        val nextIdx = (pos + 1) % tourSize

        val prev = tour[prevIdx]
        val next = tour[nextIdx]

        // Detour de insertar entre prev y neighbor
        val costBefore = prev.distance(p) + p.distance(neighbor) - prev.distance(neighbor)
        // Detour de insertar entre neighbor y next
        val costAfter = neighbor.distance(p) + p.distance(next) - neighbor.distance(next)

        return if (costBefore <= costAfter) pos else nextIdx
    }

    /**
     * Fallback: encuentra la arista del tour mas cercana a P por distancia punto-segmento.
     *
     * Complejidad: O(n) donde n = tamaño del tour
     */
    private fun findNearestEdge(
        p: Point,
        tour: List<Point>,
    ): Int {
        var bestIdx = 1
        var bestDist = Double.POSITIVE_INFINITY
        for (i in 0 until tour.size) {
            val a = tour[i]
            val b = tour[(i + 1) % tour.size]
            val dist = pointToSegmentDistance(p, a, b)
            if (dist < bestDist) {
                bestDist = dist
                bestIdx = (i + 1) % tour.size
                if (bestIdx == 0) bestIdx = tour.size
            }
        }
        return bestIdx
    }

    /**
     * Distancia perpendicular de un punto a un segmento.
     * Complejidad: O(1)
     */
    private fun pointToSegmentDistance(
        p: Point,
        a: Point,
        b: Point,
    ): Double {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0.0) return p.distance(a)

        val t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq
        val tClamped = t.coerceIn(0.0, 1.0)
        val projX = a.x + tClamped * dx
        val projY = a.y + tClamped * dy

        val ppx = p.x - projX
        val ppy = p.y - projY
        return kotlin.math.sqrt(ppx * ppx + ppy * ppy)
    }

    /**
     * Distancia minima de un punto al tour actual (min distancia a todas las aristas).
     * Complejidad: O(t) donde t = tamaño del tour
     */
    private fun minDistanceToTour(
        p: Point,
        tour: List<Point>,
    ): Double {
        var minDist = Double.POSITIVE_INFINITY
        for (i in 0 until tour.size) {
            val a = tour[i]
            val b = tour[(i + 1) % tour.size]
            val dist = pointToSegmentDistance(p, a, b)
            if (dist < minDist) minDist = dist
        }
        return minDist
    }

    /**
     * Construye el grafo de adyacencia de la triangulacion de Delaunay.
     * Complejidad: O(n log n)
     */
    private fun buildDelaunayAdjacency(points: Set<Point>): Map<Point, Set<Point>> {
        val factory = GeometryFactory()
        val builder = DelaunayTriangulationBuilder()
        builder.setSites(
            factory.createMultiPointFromCoords(
                points.map { it.toCoordinate() }.toTypedArray(),
            ),
        )

        val triangles = builder.getTriangles(factory)
        val adjacency = HashMap<Point, MutableSet<Point>>(points.size * 2)
        points.forEach { adjacency[it] = mutableSetOf() }

        val pointIndex = HashMap<Long, Point>(points.size * 2)
        for (p in points) {
            val key = java.lang.Double.doubleToLongBits(p.x) xor (java.lang.Double.doubleToLongBits(p.y) * 31)
            pointIndex[key] = p
        }

        fun findPoint(
            x: Double,
            y: Double,
        ): Point? {
            val key = java.lang.Double.doubleToLongBits(x) xor (java.lang.Double.doubleToLongBits(y) * 31)
            return pointIndex[key] ?: points.find { it.x == x && it.y == y }
        }

        for (i in 0 until triangles.numGeometries) {
            val triangle = triangles.getGeometryN(i)
            val coords = triangle.coordinates
            for (a in 0 until 3) {
                for (b in a + 1 until 3) {
                    val pA = findPoint(coords[a].x, coords[a].y) ?: continue
                    val pB = findPoint(coords[b].x, coords[b].y) ?: continue
                    adjacency[pA]!!.add(pB)
                    adjacency[pB]!!.add(pA)
                }
            }
        }

        return adjacency
    }
}
