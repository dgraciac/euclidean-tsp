package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.jts.arrayOfPoints
import com.github.dgraciac.euclideantsp.jts.centroid
import com.github.dgraciac.euclideantsp.jts.findBestIndexToInsertAt
import com.github.dgraciac.euclideantsp.jts.isClosedSimpleAndValid
import com.github.dgraciac.euclideantsp.jts.isLinearRing
import com.github.dgraciac.euclideantsp.jts.listOfPoints
import com.github.dgraciac.euclideantsp.jts.toLinearRing
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.Point

class SolverA : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        require(instance.points.size >= 3)

        val unconnectedPoints: MutableSet<Point> = instance.points.toJTSPoints().toMutableSet()

        val centroid: Point = instance.centroid()

        val unconnectedPointsSortedByDistanceToCentroid: List<Point> =
            unconnectedPoints.sortedBy { it.distance(centroid) }

        val linearRing: LinearRing = unconnectedPointsSortedByDistanceToCentroid.take(3).let {
            it.plus(it.first()).toLinearRing().also { linearRing: LinearRing ->
                if (!linearRing.isClosedSimpleAndValid()) throw RuntimeException("LinearRing not valid")
            }
        }

        linearRing.listOfPoints().dropLast(1).forEach {
            unconnectedPoints.remove(it)
                .let { removed: Boolean -> if (!removed) throw RuntimeException("Point not removed") }
        }

        val connectedPoints: ArrayList<Point> = arrayListOf(*linearRing.arrayOfPoints())

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

            val (subBestIndexToInsertAt: Int, subMinimumLength: Double) = connectedPoints.findBestIndexToInsertAt(
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
