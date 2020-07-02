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

class Christofides : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {

        return buildGraph(instance).let { graph: Graph<Point, DefaultWeightedEdge> ->
            ChristofidesThreeHalvesApproxMetricTSP<Point, DefaultWeightedEdge>().getTour(graph).toTour()
        }
    }

    private fun buildGraph(instance: Euclidean2DTSPInstance): Graph<Point, DefaultWeightedEdge> {
        return DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
            .also { graph: DefaultUndirectedWeightedGraph<Point, DefaultWeightedEdge> ->
                addVertices(instance, graph)
                addEdges(instance, graph)
            }
    }

    private fun addVertices(
        instance: Euclidean2DTSPInstance,
        graph: Graph<Point, DefaultWeightedEdge>
    ) {
        instance.points.map { point: Point -> graph.addVertex(point) }
            .forEach { if (!it) throw RuntimeException("Vertex not added") }
    }

    private fun addEdges(
        instance: Euclidean2DTSPInstance,
        graph: Graph<Point, DefaultWeightedEdge>
    ) {
        for (i: Int in 0 until instance.points.size - 1)
            for (j: Int in i + 1 until instance.points.size) {
                val point1: Point = instance.points[i]
                val point2: Point = instance.points[j]
                graph.addEdge(point1, point2).let { defaultWeightedEdge: DefaultWeightedEdge? ->
                    when (defaultWeightedEdge) {
                        null -> throw RuntimeException("Edge not added")
                        else -> graph.setEdgeWeight(defaultWeightedEdge, point1.distance(point2))
                    }
                }
            }
    }
}

private fun GraphPath<Point, DefaultWeightedEdge>.toTour(): Tour =
    Tour(listOf<Point>(*this.vertexList.toTypedArray()))
