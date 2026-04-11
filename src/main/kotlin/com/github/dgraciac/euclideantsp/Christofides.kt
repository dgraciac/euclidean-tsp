package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Point
import com.github.dgraciac.euclideantsp.shared.Tour
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.tour.ChristofidesThreeHalvesApproxMetricTSP
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge

/**
 * Christofides — Algoritmo de Christofides (implementacion de JGraphT)
 *
 * Linea de investigacion: Ninguna (baseline de referencia con garantia de aproximacion 1.5x)
 *
 * Algoritmo:
 * 1. Construye un grafo completo no dirigido ponderado con distancias euclideas — O(n^2)
 * 2. Calcula el arbol generador minimo (MST) — O(n^2 log n)
 * 3. Encuentra un matching de peso minimo sobre los vertices de grado impar — O(n^3)
 * 4. Combina MST + matching para formar un multigrafo euleriano
 * 5. Encuentra un circuito euleriano y lo convierte en tour hamiltoniano (shortcutting)
 *
 * Complejidad e2e: O(n^3)
 * - Paso 1: O(n^2) — construccion del grafo completo
 * - Paso 2: O(n^2) — MST con Prim sobre grafo denso
 * - Paso 3: O(n^3) — matching de peso minimo (dominante)
 * - Pasos 4-5: O(n)
 *
 * Resultados:
 *   berlin52: ratio=1.118, tiempo=0.06s
 *   st70:     ratio=1.130, tiempo=0.03s
 *   kro200:   ratio=1.156, tiempo=0.04s
 *   a280:     ratio=1.143, tiempo=0.10s
 *
 * Metricas agregadas: Media aritmetica=1.137x | Media geometrica=1.137x | Peor caso=1.156x
 */
class Christofides : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour =
        buildGraph(instance).let { graph: Graph<Point, DefaultWeightedEdge> ->
            ChristofidesThreeHalvesApproxMetricTSP<Point, DefaultWeightedEdge>().getTour(graph).toTour()
        }

    private fun buildGraph(instance: Euclidean2DTSPInstance): Graph<Point, DefaultWeightedEdge> =
        DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
            .addVertices(instance)
            .addEdges(instance)

    private fun Graph<Point, DefaultWeightedEdge>.addVertices(instance: Euclidean2DTSPInstance): Graph<Point, DefaultWeightedEdge> {
        instance.points.map { point: Point ->
            this.addVertex(point).let { if (!it) throw RuntimeException("Vertex not added") }
        }
        return this
    }

    private fun Graph<Point, DefaultWeightedEdge>.addEdges(instance: Euclidean2DTSPInstance): Graph<Point, DefaultWeightedEdge> {
        for (i: Int in 0 until instance.points.size - 1) {
            for (j: Int in i + 1 until instance.points.size) {
                val point1: Point = instance.points.elementAt(i)
                val point2: Point = instance.points.elementAt(j)
                this.addEdge(point1, point2).let { defaultWeightedEdge: DefaultWeightedEdge? ->
                    when (defaultWeightedEdge) {
                        null -> throw RuntimeException("Edge not added")
                        else -> this.setEdgeWeight(defaultWeightedEdge, point1.distance(point2))
                    }
                }
            }
        }
        return this
    }
}

private fun GraphPath<Point, DefaultWeightedEdge>.toTour(): Tour = Tour(listOf<Point>(*this.vertexList.toTypedArray()))
