package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.jts.arrayOfPoints
import com.github.dgraciac.euclideantsp.jts.findBestIndexToInsertAt2
import com.github.dgraciac.euclideantsp.jts.isLinearRing
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.algorithm.ConvexHull
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point

class SolverB : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val unconnectedPoints: MutableSet<Point> = instance.points.toJTSPoints().toMutableSet()

        val convexHullNoGeometry = ConvexHull(instance.points.toCoordinates().toTypedArray(), GeometryFactory())
        val convexHullGeometry: Geometry = convexHullNoGeometry.convexHull

        val connectedPoints: ArrayList<Point> = arrayListOf(*convexHullGeometry.arrayOfPoints())
            convexHullGeometry.coordinates.map { coordinate: Coordinate -> coordinate.toJTSPoint() }.toTypedArray()

        connectedPoints.dropLast(1).forEach {
            unconnectedPoints.remove(it)
                .let { removed: Boolean -> if (!removed) throw RuntimeException("Point not removed") }
        }

        while (unconnectedPoints.isNotEmpty()) {
            val bestInsertion: Pair<Point, Int> = findBestInsertion(unconnectedPoints, connectedPoints)

            connectedPoints.add(bestInsertion.second, bestInsertion.first)
            ensureLinearRing(connectedPoints)

            unconnectedPoints.remove(bestInsertion.first)
        }

        return Tour(points = connectedPoints.map { com.github.dgraciac.euclideantsp.shared.Point(it.x, it.y) })
    }

    private fun ensureLinearRing(connectedPoints: ArrayList<Point>) {
        if (!connectedPoints.isLinearRing()) throw RuntimeException("Connected points are not a Linear Ring")
    }

    private fun findBestInsertion(
        unconnectedPoints: MutableSet<Point>,
        connectedPoints: ArrayList<Point>
    ): Pair<Point, Int> {

        var bestUnconnected: Point? = null
        var bestIndexToInsertAt: Int? = null
        var minimumLength: Double = Double.POSITIVE_INFINITY

        unconnectedPoints.forEach { unconnectedPoint: Point ->

            val (subBestIndexToInsertAt: Int, subMinimumLength: Double) = connectedPoints.findBestIndexToInsertAt2(
                unconnectedPoint
            )

            if (subMinimumLength < minimumLength) {
                bestUnconnected = unconnectedPoint
                bestIndexToInsertAt = subBestIndexToInsertAt
                minimumLength = subMinimumLength
            }
        }

        if (bestUnconnected == null) throw RuntimeException("Best Unconnected is null")
        if (bestIndexToInsertAt == null) throw RuntimeException("Best Index is null")
        return Pair(bestUnconnected!!, bestIndexToInsertAt!!)
    }
}
