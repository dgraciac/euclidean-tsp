package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.jts.lengthAfterInsertBetweenPairOfPoints
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import com.google.common.collect.Sets
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

class SolverA : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        if (instance.points.size == 3) return Tour(instance.points)

        val geometryFactory = GeometryFactory()
        val unconnectedPoints: MutableSet<Point> = instance.points.toJTSPoints().toMutableSet()

        val combinationsOf3Points: List<MutableList<Point>> =
            Sets.combinations(unconnectedPoints.toSet(), 3).map { it.toMutableList() }

        val polygon: Polygon =
            combinationsOf3Points.map { it.toTypedArray().plus(it[0]) }.map {
                it.map { point: Point -> point.coordinate }
                    .toTypedArray()
                    .let { arrayOfCoordinates: Array<Coordinate> -> geometryFactory.createPolygon(arrayOfCoordinates) }
            }.find { triangle: Polygon -> unconnectedPoints.none { triangle.contains(it) } }
                ?: throw RuntimeException("Triangle not found")

        unconnectedPoints.removeAll(polygon.coordinates.map { it.toJTSPoint() })
            .let { removed -> if (!removed) throw RuntimeException("Points not removed") }

        val connectedPoints: ArrayList<Point> = arrayListOf(*polygon.coordinates.map { it.toJTSPoint() }.toTypedArray())
        connectedPoints.removeAt(connectedPoints.size - 1)

        while (unconnectedPoints.isNotEmpty()) {
            val bestInsertion: Pair<Point, Pair<Point, Point>> =
                findBestInsertion(unconnectedPoints, connectedPoints)

            connectedPoints.add(connectedPoints.indexOf(bestInsertion.second.second), bestInsertion.first)
            unconnectedPoints.remove(bestInsertion.first)
        }

        return Tour(points = connectedPoints.map { com.github.dgraciac.euclideantsp.shared.Point(it.x, it.y) })
    }

    private fun findBestInsertion(
        unconnectedPoints: MutableSet<Point>,
        connectedPoints: ArrayList<Point>
    ): Pair<Point, Pair<Point, Point>> {

        val bestUnconnected: Point = unconnectedPoints.minBy { unconnectedPoint: Point ->
            val pairs: MutableList<Pair<Point, Point>> = connectedPoints
                .zipWithNext()
                .toMutableList()
                .also { it.add(Pair(connectedPoints.last(), connectedPoints.first())) }
            pairs.minBy { pair: Pair<Point, Point> ->
                lengthAfterInsertBetweenPairOfPoints(pair, unconnectedPoint)
            }?.let {
                lengthAfterInsertBetweenPairOfPoints(it, unconnectedPoint)
            } ?: throw RuntimeException("Null Pair")
        } ?: throw RuntimeException("Null Best Unconnected")

        val pairs: MutableList<Pair<Point, Point>> = connectedPoints
            .zipWithNext()
            .toMutableList()
            .also { it.add(Pair(connectedPoints.last(), connectedPoints.first())) }

        val bestPair: Pair<Point, Point> = pairs.minBy { pair: Pair<Point, Point> ->
            lengthAfterInsertBetweenPairOfPoints(pair, bestUnconnected)
        } ?: throw RuntimeException("Null best pair")

        return Pair(bestUnconnected, bestPair)
    }
}