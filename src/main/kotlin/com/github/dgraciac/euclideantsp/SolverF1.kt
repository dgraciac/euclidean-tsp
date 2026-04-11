package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder

/**
 * SolverF1 — Delaunay nearest neighbor + 2-opt + or-opt + 2-opt
 *
 * Linea de investigacion: F (basado en triangulacion de Delaunay)
 * Experimento: E010
 *
 * Hipotesis: El tour optimo tiende a usar aristas de la triangulacion de Delaunay.
 * Usar la triangulacion como grafo de vecindad para construir un tour con nearest
 * neighbor restringido a aristas Delaunay, y luego aplicar busqueda local,
 * producira tours de alta calidad.
 *
 * Algoritmo:
 * 1. Calcular triangulacion de Delaunay — O(n log n)
 * 2. Construir grafo de adyacencia desde la triangulacion
 * 3. Nearest neighbor sobre el grafo Delaunay: siempre ir al vecino Delaunay
 *    mas cercano no visitado. Si no quedan vecinos Delaunay, ir al punto
 *    mas cercano global (fallback) — O(n^2) peor caso
 * 4. 2-opt + or-opt + 2-opt — O(n^3)
 *
 * Complejidad e2e: O(n^3)
 * Complejidad peor caso: O(n^3) — Delaunay O(n log n) + NN O(n^2) + pipeline O(n^3)
 *
 * Resultados:
 *   berlin52: ratio=1.053, tiempo=0.054s
 *   st70:     ratio=1.047, tiempo=0.009s
 *   kro200:   ratio=1.061, tiempo=0.031s
 *   a280:     ratio=1.065, tiempo=0.028s
 *
 * Metricas agregadas: Media aritmetica=1.057x | Media geometrica=1.057x | Peor caso=1.065x
 *
 * Conclusion: Peor que SolverE1 (NN global). La restriccion a aristas Delaunay empeora la
 * semilla en vez de mejorarla, ya que NN global tiende a elegir aristas Delaunay naturalmente.
 * Delaunay podria ser mas util en la fase de busqueda local que en la construccion.
 */
class SolverF1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1-2: Triangulacion de Delaunay + grafo de adyacencia
        val adjacency = buildDelaunayAdjacency(instance.points)

        // Paso 3: Nearest neighbor sobre grafo Delaunay
        val nnTour = delaunayNearestNeighbor(instance.points, adjacency)

        // Paso 4: 2-opt -> or-opt -> 2-opt
        val afterTwoOpt = twoOpt(nnTour)
        val afterOrOpt = orOpt(afterTwoOpt)
        val finalTour = twoOpt(afterOrOpt)

        return Tour(points = finalTour)
    }

    /**
     * Construye el grafo de adyacencia de la triangulacion de Delaunay.
     *
     * @param points conjunto de puntos
     * @return mapa de punto -> conjunto de vecinos Delaunay
     * Complejidad: O(n log n) para la triangulacion + O(n) para extraer aristas
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
        val adjacency = mutableMapOf<Point, MutableSet<Point>>()

        // Inicializar todos los puntos
        points.forEach { adjacency[it] = mutableSetOf() }

        // Extraer aristas de cada triangulo
        for (i in 0 until triangles.numGeometries) {
            val triangle = triangles.getGeometryN(i)
            val coords = triangle.coordinates
            // Un triangulo tiene 4 coordenadas (cerrado), las 3 primeras son vertices
            for (a in 0 until 3) {
                for (b in a + 1 until 3) {
                    val pA = findPoint(points, coords[a].x, coords[a].y) ?: continue
                    val pB = findPoint(points, coords[b].x, coords[b].y) ?: continue
                    adjacency.getOrPut(pA) { mutableSetOf() }.add(pB)
                    adjacency.getOrPut(pB) { mutableSetOf() }.add(pA)
                }
            }
        }

        return adjacency
    }

    /**
     * Nearest neighbor restringido a aristas Delaunay, con fallback a global.
     * Complejidad: O(n^2) peor caso (por los fallbacks)
     */
    private fun delaunayNearestNeighbor(
        points: Set<Point>,
        adjacency: Map<Point, Set<Point>>,
    ): List<Point> {
        val remaining = points.toMutableSet()
        val tour = mutableListOf<Point>()

        val start = remaining.first()
        tour.add(start)
        remaining.remove(start)

        while (remaining.isNotEmpty()) {
            val current = tour.last()
            val neighbors = adjacency[current]?.filter { it in remaining }

            val next =
                if (!neighbors.isNullOrEmpty()) {
                    // Preferir vecino Delaunay mas cercano
                    neighbors.minBy { it.distance(current) }
                } else {
                    // Fallback: punto global mas cercano
                    remaining.minBy { it.distance(current) }
                }

            tour.add(next)
            remaining.remove(next)
        }

        tour.add(tour.first())
        return tour
    }

    private fun findPoint(
        points: Set<Point>,
        x: Double,
        y: Double,
    ): Point? = points.find { it.x == x && it.y == y }
}
