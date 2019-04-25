package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolverA implements Euclidean2DTSPSolver {

    @Override
    public Tour compute(Euclidean2DTSPInstance instance) {
        Coordinate[] coordinates = instance.coordinates.toArray(new Coordinate[instance.coordinates.size()]);

        ConvexHull convexHullNoGeom = new ConvexHull(coordinates, new GeometryFactory());
        Geometry convexHull = convexHullNoGeom.getConvexHull();
        List<Coordinate> listOfConnectedCoordinates = getListOfConnectedCoordinates(convexHull);


        Set<Coordinate> setOfConnectedCoordinates = Set.copyOf(listOfConnectedCoordinates);
        List<Coordinate> listOfUnconnectedCoordinates = Set.of(coordinates).stream()
                .filter(coordinate -> !setOfConnectedCoordinates.contains(coordinate)).distinct().collect(Collectors.toList());

        while (!listOfUnconnectedCoordinates.isEmpty()) {
            if (listOfUnconnectedCoordinates.size() > 2) {

            //} else if (listOfUnconnectedCoordinates.size() > 1) {
/*                List<Coordinate> remainingCoordinates = new ArrayList<>(listOfUnconnectedCoordinates);
                Tour candidateTour = TourMapper.toTour(listOfConnectedCoordinates);
                candidateTour.insertPathAtCheapestPosition(remainingCoordinates);*/
            } else {
                Coordinate coordinate = listOfUnconnectedCoordinates.get(0);
                int position = findCheapestPositionInTheCurrentTour(listOfConnectedCoordinates, coordinate);
                listOfConnectedCoordinates.add(position, coordinate);
                listOfUnconnectedCoordinates.remove(coordinate);
            }
        }

        return new Tour(listOfConnectedCoordinates);
    }

    private List<Coordinate> getListOfConnectedCoordinates(Geometry convexHull) {
        Coordinate[] coordinatesArray = convexHull.getCoordinates();
        List<Coordinate> coordinates = new ArrayList<>(Arrays.asList(coordinatesArray));
        int size = coordinates.size();
        int lastElement = size - 1;
        if(size > 1 && coordinates.get(0).equals2D(coordinates.get(lastElement))) {
            coordinates.remove(lastElement);
        }
        return coordinates;
    }

    private int findCheapestPositionInTheCurrentTour(List<Coordinate> coordinates, Coordinate coordinate) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        int position = -1;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate firstCoordinate = coordinates.get(i);
            Coordinate secondCoordinate = coordinates.get(i + 1);
            double distance = firstCoordinate.distance(coordinate) + coordinate.distance(secondCoordinate);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                position = i + 1;
            }
        }
        Coordinate lastCoordinate = coordinates.get(coordinates.size() - 1);
        Coordinate firstCoordinate = coordinates.get(0);
        double distance = lastCoordinate.distance(coordinate) + coordinate.distance(firstCoordinate);
        if (distance < minimumDistance) {
            position = 0;
        }
        return position;
    }
}
