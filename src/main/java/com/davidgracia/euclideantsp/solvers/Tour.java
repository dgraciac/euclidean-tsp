package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.*;

public class Tour {
    private final List<Coordinate> coordinates;
    private static final double NOT_CALCULATED_YET = -1;
    private double distance;

    public Tour(Coordinate coordinate, Coordinate... otherCoordinates) {
        List<Coordinate> tempCoordinates = new ArrayList<>();
        tempCoordinates.add(coordinate);
        tempCoordinates.addAll(Arrays.asList(otherCoordinates));
        coordinates = Collections.unmodifiableList(tempCoordinates);
        distance = NOT_CALCULATED_YET;
    }

    public Tour(Coordinate[] coordinates) {
        this.coordinates = List.of(coordinates);
        distance = NOT_CALCULATED_YET;
    }

    public Tour(List<Coordinate> listOfConnectedCoordinates) {
        coordinates = Collections.unmodifiableList(listOfConnectedCoordinates);
        distance = NOT_CALCULATED_YET;
    }

    public double getDistance() {
        return distance == NOT_CALCULATED_YET ?
                distance = DistanceCalculator.calculateTourLength(this.coordinates) : distance;
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

    public Tour newTourAfterInsertingCoordinateAtCheapestPosition(Coordinate coordinate) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        int secondPosition = -1;
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            Coordinate firstConnectedCoordinate = this.coordinates.get(i);
            Coordinate secondConnectedCoordinate = this.coordinates.get(i + 1);
            double distance = firstConnectedCoordinate.distance(coordinate)
                    + coordinate.distance(secondConnectedCoordinate);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                secondPosition = i + 1;
            }
        }
        double distance = this.coordinates.get(this.coordinates.size() - 1).distance(coordinate)
                + coordinate.distance(this.coordinates.get(0));
        if (distance < minimumDistance) {
            secondPosition = 0;
        }

        List<Coordinate> coordinatesForNewTour = new ArrayList<>(this.coordinates);
        coordinatesForNewTour.add(secondPosition, coordinate);
        return new Tour(coordinatesForNewTour);
    }

    public Tour newTourAfterInsertingPathAtCheapestPosition(List<Coordinate> coordinates) {
        GeometryFactory geometryFactory = new GeometryFactory();
        LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
        double lineStringLength = lineString.getLength();

        double minimumDistance = Double.POSITIVE_INFINITY;
        int secondPosition = -1;
        boolean reverse = false;
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            Coordinate firstConnectedCoordinate = this.coordinates.get(i);
            Coordinate secondConnectedCoordinate = this.coordinates.get(i + 1);
            double distance = firstConnectedCoordinate.distance(lineString.getStartPoint().getCoordinate())
                    + lineStringLength
                    + lineString.getEndPoint().getCoordinate().distance(secondConnectedCoordinate);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                secondPosition = i + 1;
                reverse = false;
            }

            distance = firstConnectedCoordinate.distance(lineString.getEndPoint().getCoordinate())
                    + lineStringLength
                    + lineString.getStartPoint().getCoordinate().distance(secondConnectedCoordinate);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                secondPosition = i + 1;
                reverse = true;
            }
        }
        double distance = this.coordinates.get(this.coordinates.size() - 1).distance(lineString.getStartPoint().getCoordinate())
                + lineStringLength
                + lineString.getEndPoint().getCoordinate().distance(this.coordinates.get(0));
        if (distance < minimumDistance) {
            minimumDistance = distance;
            secondPosition = 0;
            reverse = false;
        }

        distance = this.coordinates.get(this.coordinates.size() - 1).distance(lineString.getEndPoint().getCoordinate())
                + lineStringLength
                + lineString.getStartPoint().getCoordinate().distance(this.coordinates.get(0));
        if (distance < minimumDistance) {
            secondPosition = 0;
            reverse = true;
        }

        if (reverse) {
            Collections.reverse(coordinates);
        }

        List<Coordinate> coordinatesForNewTour = new ArrayList<>(this.coordinates);
        coordinatesForNewTour.addAll(secondPosition, coordinates);
        return new Tour(coordinatesForNewTour);
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
    }
}
