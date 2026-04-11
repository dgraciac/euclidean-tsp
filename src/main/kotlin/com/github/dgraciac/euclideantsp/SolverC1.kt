package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.GeometryFactory

/**
 * SolverC1 — Convex hull peeling + insercion por ratio
 *
 * Linea de investigacion: C (descomposicion en capas de convex hull)
 * Padre: SolverC (concepto)
 * Experimento: E002
 *
 * Hipotesis: Descomponer los puntos en capas concentricas de convex hull
 * y luego insertar las capas interiores preservara mejor la estructura
 * geometrica que empezar desde un unico convex hull.
 *
 * Algoritmo:
 * 1. Pelar convex hulls sucesivos (onion peeling): — O(n^2 log n) peor caso
 *    a. Calcular convex hull de los puntos restantes — O(n log n)
 *    b. Extraer los puntos del hull como una capa
 *    c. Eliminar esos puntos del conjunto y repetir
 * 2. Usar la capa exterior como tour inicial — O(1)
 * 3. Para cada punto de las capas interiores (de exterior a interior):
 *    buscar la mejor posicion de insercion en el tour actual
 *    minimizando el ratio (d(A,P)+d(P,B))/d(A,B) — O(n) por punto
 * 4. Repetir hasta insertar todos los puntos
 *
 * Complejidad e2e: O(n^3)
 * - Paso 1: O(k * n log n) donde k = numero de capas, peor caso O(n^2 log n)
 * - Paso 3-4: O(m) puntos interiores * O(n) busqueda por insercion = O(n^2)
 *   (Nota: aqui NO usamos findBestIndexToInsertAt2 con su validacion isLinearRing,
 *    sino una insercion directa sin validacion, reduciendo de O(n^2) a O(n) por punto)
 * - Total: O(n^2 log n) o O(n^3) si hay muchas capas
 *
 * Resultados:
 *   berlin52: ratio=1.147, tiempo=0.003s (vs Christofides 1.118: peor)
 *   st70:     ratio=1.123, tiempo=0.001s (vs Christofides 1.130: ligeramente mejor)
 *   kro200:   ratio=1.206, tiempo=0.003s (vs Christofides 1.156: peor)
 *   a280:     ratio=1.218, tiempo=0.004s (vs Christofides 1.143: peor)
 *
 * Metricas agregadas: Media aritmetica=1.174x | Media geometrica=1.173x | Peor caso=1.218x
 *
 * Conclusion: Rapido pero peor que Christofides en general. La insercion simple de
 * puntos interiores no preserva la estructura geometrica de las capas. El peeling en si
 * es prometedor (O(n^2 log n)) pero necesita mejor estrategia de conexion entre capas.
 */
class SolverC1 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        // Paso 1: Convex hull peeling — obtener capas concentricas
        val layers = peelConvexHulls(instance.points)

        // Paso 2: Tour inicial con la capa exterior
        val tourPoints = layers.first().toMutableList()
        tourPoints.add(tourPoints.first()) // Cerrar el tour

        // Paso 3-4: Insertar puntos de capas interiores
        for (layerIndex in 1 until layers.size) {
            val layer = layers[layerIndex]
            for (point in layer) {
                val bestIndex = findBestInsertionIndex(tourPoints, point)
                tourPoints.add(bestIndex, point)
            }
        }

        return Tour(points = tourPoints)
    }

    /**
     * Descompone un conjunto de puntos en capas concentricas de convex hull.
     * Cada capa contiene los puntos del convex hull del conjunto restante.
     * Los puntos dentro de cada capa mantienen el orden del hull (sentido antihorario).
     *
     * @param points conjunto de puntos a descomponer
     * @return lista de capas, de exterior a interior, cada capa es una lista ordenada de puntos
     * Complejidad: O(k * n log n) donde k = numero de capas. Peor caso O(n^2 log n)
     */
    private fun peelConvexHulls(points: Set<Point>): List<List<Point>> {
        val layers = mutableListOf<List<Point>>()
        val remaining = points.toMutableSet()

        while (remaining.size >= 3) {
            val coordinates = remaining.map { it.toCoordinate() }.toTypedArray()
            val hull = ConvexHull(coordinates, GeometryFactory()).convexHull

            // Extraer puntos del hull (sin el ultimo duplicado que cierra el poligono)
            val hullCoordinates = hull.coordinates.dropLast(1)
            val hullPoints =
                hullCoordinates.map { coord ->
                    remaining.first { it.x == coord.x && it.y == coord.y }
                }

            if (hullPoints.size < 3) {
                // Los puntos restantes son colineales o hay menos de 3 — añadir como ultima capa
                layers.add(remaining.toList())
                remaining.clear()
            } else {
                layers.add(hullPoints)
                remaining.removeAll(hullPoints.toSet())
            }
        }

        // Puntos restantes (1 o 2) que no forman un hull
        if (remaining.isNotEmpty()) {
            layers.add(remaining.toList())
        }

        return layers
    }

    /**
     * Encuentra el mejor indice para insertar un punto en el tour actual.
     * Criterio: minimiza el ratio (d(A,P) + d(P,B)) / d(A,B) donde A y B son
     * puntos consecutivos en el tour.
     *
     * @param tour lista de puntos del tour (cerrado: primero == ultimo)
     * @param point punto a insertar
     * @return indice donde insertar el punto
     * Complejidad: O(n) donde n = tamaño del tour
     */
    private fun findBestInsertionIndex(
        tour: List<Point>,
        point: Point,
    ): Int {
        var bestIndex = 1
        var bestRatio = Double.POSITIVE_INFINITY

        for (i in 0 until tour.size - 1) {
            val a = tour[i]
            val b = tour[i + 1]
            val distAB = a.distance(b)
            if (distAB == 0.0) continue

            val insertionDist = a.distance(point) + point.distance(b)
            val ratio = insertionDist / distAB

            if (ratio < bestRatio) {
                bestRatio = ratio
                bestIndex = i + 1
            }
        }

        return bestIndex
    }
}
