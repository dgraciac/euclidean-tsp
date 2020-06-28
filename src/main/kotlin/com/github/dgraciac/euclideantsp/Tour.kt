package com.github.dgraciac.euclideantsp

import org.locationtech.jts.geom.Coordinate
import java.util.*
import java.util.function.Consumer

data class Tour(val coordinates: List<Coordinate>, val distance: Double) {

/*    constructor(coordinate: Coordinate, vararg otherCoordinates: Coordinate?) {
        val tempCoordinates: MutableList<Coordinate> = ArrayList()
        tempCoordinates.add(coordinate)
        tempCoordinates.addAll(Arrays.asList(*otherCoordinates))
        coordinates = Collections.unmodifiableList(tempCoordinates)
        distance = DistanceCalculator.calculateTourLength(coordinates)
    }

    internal constructor(coordinates: Array<Coordinate?>?) {
        this.coordinates = java.util.List.of(coordinates)
        distance = DistanceCalculator.calculateTourLength(this.coordinates)
    }

    internal constructor(listOfConnectedCoordinates: List<Coordinate?>?) {
        coordinates = Collections.unmodifiableList(listOfConnectedCoordinates)
        distance = DistanceCalculator.calculateTourLength(coordinates)
    }*/

/*    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val otherTour = o as Tour
        var areEquals = false
        var i = 0
        val tempPoints =
            Arrays.asList(*arrayOfNulls<Coordinate>(coordinates.size))
        Collections.copy(tempPoints, otherTour.coordinates)
        while (i < coordinates.size && !areEquals) {
            Collections.rotate(tempPoints, 1)
            areEquals = coordinates == tempPoints
            Collections.reverse(tempPoints)
            areEquals = areEquals || coordinates == tempPoints
            Collections.reverse(tempPoints)
            i++
        }
        return areEquals
    }

    fun cheapestTourAfterInsertingBestCoordinateOf(coordinates: List<Coordinate>): Tour? {
        var minimumDistance = Double.POSITIVE_INFINITY
        var cheapestTour: Tour? = null
        for (coordinate in coordinates) {
            val tour = cheapestTourAfterInsertingCoordinate(coordinate)
            val candidateTourDistance: Double = tour.getDistance()
            if (candidateTourDistance < minimumDistance) {
                minimumDistance = candidateTourDistance
                cheapestTour = tour
            }
        }
        return cheapestTour
    }

    private fun cheapestTourAfterInsertingCoordinate(coordinate: Coordinate): Tour {
        val position = CheapestTourFinder.findCheapestPositionForGivenCoordinate(coordinates, coordinate)
        return buildTourMerging(coordinates, coordinate, position)
    }

    private fun buildTourMerging(
        coordinates: List<Coordinate?>,
        coordinateToMerge: Coordinate,
        position: Int
    ): Tour {
        val coordinatesToMerge: List<Coordinate?> = java.util.List.of(coordinateToMerge)
        return buildTourMerging(coordinates, coordinatesToMerge, position)
    }

    private fun buildTourMerging(
        firstCoordinates: List<Coordinate?>,
        coordinatesToMerge: List<Coordinate?>,
        position: Int
    ): Tour {
        val coordinatesForNewTour: MutableList<Coordinate?> =
            ArrayList(firstCoordinates)
        coordinatesForNewTour.addAll(position, coordinatesToMerge)
        return Tour(coordinatesForNewTour)
    }

    private fun cheapestTourAfterInsertingPathDirectionSensitive(coordinates: List<Coordinate?>): Tour {
        val position =
            CheapestTourFinder.findCheapestPositionForGivenPathDirectionSensitive(this.coordinates, coordinates)
        return buildTourMerging(this.coordinates, coordinates, position)
    }

    fun cheapestTourAfterInsertingPath(coordinates: List<Coordinate?>): Tour {
        val bestTour: Tour
        val candidateTourA = cheapestTourAfterInsertingPathDirectionSensitive(coordinates)
        val reversedCoordinates: List<Coordinate?> = ArrayList(coordinates)
        Collections.reverse(reversedCoordinates)
        val candidateTourB = cheapestTourAfterInsertingPathDirectionSensitive(reversedCoordinates)
        bestTour = if (candidateTourA.getDistance() < candidateTourB.getDistance()) candidateTourA else candidateTourB
        return bestTour
    }

    override fun hashCode(): Int {
        //TODO
        return Objects.hash(coordinates)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Tour{")
        coordinates.forEach(Consumer { coordinate: Coordinate? ->
            stringBuilder.append('(').append(coordinate!!.x).append(',').append(coordinate.y).append(')')
        })
        stringBuilder.append(", d=").append(distance).append("}")
        return stringBuilder.toString()
    }*/
}