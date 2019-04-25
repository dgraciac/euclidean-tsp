package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

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
        return distance == NOT_CALCULATED_YET ? distance = calculateDistance(this.coordinates) : distance;
    }

    private double calculateDistance(List<Coordinate> coordinates) {
        double distance = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            distance += coordinates.get(i).distance(coordinates.get(i + 1));
        }
        distance += coordinates.get(coordinates.size() - 1).distance(coordinates.get(0));
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

    @Override
    public int hashCode() {
        //TODO
        return Objects.hash(coordinates);
    }

    @Override
    public String toString() {
        return "Tour{" + coordinates + ", d=" + getDistance() + "}";
    }

    public void insertPathAtCheapestPosition(List<Coordinate> coordinates) {
        GeometryFactory f = new GeometryFactory();
        //
        double minimumDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
//            Coordinate firstConnectedCoordinate = CoordinateMapper.toCoordinate(this.coordinates.get(i));
            //double distance = firstConnectedCoordinate.distance()
        }
    }
}
