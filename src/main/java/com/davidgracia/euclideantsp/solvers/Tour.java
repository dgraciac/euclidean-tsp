package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;

import java.util.*;

public class Tour {
/*    private final List<Coordinate> coordinates;
    private final double distance;

    public Tour(Coordinate coordinate, Coordinate... otherCoordinates) {
        List<Coordinate> tempCoordinates = new ArrayList<>();
        tempCoordinates.add(coordinate);
        tempCoordinates.addAll(Arrays.asList(otherCoordinates));
        coordinates = Collections.unmodifiableList(tempCoordinates);
        distance = DistanceCalculator.calculateTourLength(this.coordinates);
    }

    Tour(Coordinate[] coordinates) {
        this.coordinates = List.of(coordinates);
        distance = DistanceCalculator.calculateTourLength(this.coordinates);
    }

    Tour(List<Coordinate> listOfConnectedCoordinates) {
        coordinates = Collections.unmodifiableList(listOfConnectedCoordinates);
        distance = DistanceCalculator.calculateTourLength(this.coordinates);
    }

    public double getDistance() {
        return distance;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tour otherTour = (Tour) o;

        boolean areEquals = false;
        int i = 0;
        List<Coordinate> tempPoints = Arrays.asList(new Coordinate[coordinates.size()]);
        Collections.copy(tempPoints, otherTour.coordinates);
        while (i < coordinates.size() && !areEquals) {
            Collections.rotate(tempPoints, 1);
            areEquals = Objects.equals(coordinates, tempPoints);
            Collections.reverse(tempPoints);
            areEquals = areEquals || Objects.equals(coordinates, tempPoints);
            Collections.reverse(tempPoints);
            i++;
        }
        return areEquals;
    }

    Tour cheapestTourAfterInsertingBestCoordinateOf(List<Coordinate> coordinates) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        Tour cheapestTour = null;
        for (Coordinate coordinate : coordinates) {
            Tour tour = cheapestTourAfterInsertingCoordinate(coordinate);
            double candidateTourDistance = tour.getDistance();
            if(candidateTourDistance < minimumDistance) {
                minimumDistance = candidateTourDistance;
                cheapestTour = tour;
            }
        }
        return cheapestTour;
    }

    private Tour cheapestTourAfterInsertingCoordinate(Coordinate coordinate) {
        int position = CheapestTourFinder.findCheapestPositionForGivenCoordinate(this.coordinates, coordinate);
        return buildTourMerging(coordinates, coordinate, position);
    }

    private Tour buildTourMerging(List<Coordinate> coordinates, Coordinate coordinateToMerge, int position) {
        List<Coordinate> coordinatesToMerge = List.of(coordinateToMerge);
        return buildTourMerging(coordinates,coordinatesToMerge,position);
    }

    private Tour buildTourMerging(List<Coordinate> firstCoordinates, List<Coordinate> coordinatesToMerge, int position) {
        List<Coordinate> coordinatesForNewTour = new ArrayList<>(firstCoordinates);
        coordinatesForNewTour.addAll(position, coordinatesToMerge);
        return new Tour(coordinatesForNewTour);
    }

    private Tour cheapestTourAfterInsertingPathDirectionSensitive(List<Coordinate> coordinates) {
        int position = CheapestTourFinder.findCheapestPositionForGivenPathDirectionSensitive(this.coordinates, coordinates);
        return buildTourMerging(this.coordinates, coordinates, position);
    }

    Tour cheapestTourAfterInsertingPath(List<Coordinate> coordinates) {
        Tour bestTour;
        Tour candidateTourA = this.cheapestTourAfterInsertingPathDirectionSensitive(coordinates);

        List<Coordinate> reversedCoordinates = new ArrayList<>(coordinates);
        Collections.reverse(reversedCoordinates);
        Tour candidateTourB = this.cheapestTourAfterInsertingPathDirectionSensitive(reversedCoordinates);
        if (candidateTourA.getDistance() < candidateTourB.getDistance()) bestTour = candidateTourA;
        else bestTour = candidateTourB;

        return bestTour;
    }

    @Override
    public int hashCode() {
        //TODO
        return Objects.hash(coordinates);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Tour{");
        coordinates.forEach(coordinate -> stringBuilder.append('(').append(coordinate.x).append(',').append(coordinate.y).append(')'));
        stringBuilder.append(", d=").append(getDistance()).append("}");
        return stringBuilder.toString();
    }*/
}
