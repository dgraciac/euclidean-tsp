package com.github.dgraciac.euclideantsp

import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.tour.ChristofidesThreeHalvesApproxMetricTSP
import org.jgrapht.graph.SimpleGraph

class Christofides : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {

        return buildGraph(instance).let { graph: Graph<Point, Double> ->
            ChristofidesThreeHalvesApproxMetricTSP<Point, Double>().getTour(graph).toTour()
        }
    }

    private fun buildGraph(instance: Euclidean2DTSPInstance): Graph<Point, Double> {
        return SimpleGraph<Point, Double>(Double::class.java)
            .apply { addVertices(instance, this) }
            .apply { addEdges(instance, this) }
    }

    private fun addEdges(
        instance: Euclidean2DTSPInstance,
        graph: Graph<Point, Double>
    ) {
        instance.coordinates.forEach { point1: Point ->
            instance.coordinates.forEach { point2: Point ->
                if (point1 != point2) graph.addEdge(point1, point2, point1.distance(point2))
            }
        }
    }

    private fun addVertices(
        instance: Euclidean2DTSPInstance,
        graph: Graph<Point, Double>
    ) {
        instance.coordinates.forEach { point: Point -> graph.addVertex(point) }
    }
}

private fun GraphPath<Point, Double>.toTour(): Tour = Tour(listOf<Point>(*this.vertexList.toTypedArray()))
