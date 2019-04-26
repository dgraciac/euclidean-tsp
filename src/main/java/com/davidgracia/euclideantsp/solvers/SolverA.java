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
        Coordinate[] coordinates = instance.coordinates.toArray(new Coordinate[0]);

        ConvexHull convexHullNoGeom = new ConvexHull(coordinates, new GeometryFactory());
        Geometry convexHull = convexHullNoGeom.getConvexHull();
        List<Coordinate> listOfConnectedCoordinates = getListOfConnectedCoordinates(convexHull);

        Set<Coordinate> setOfConnectedCoordinates = Set.copyOf(listOfConnectedCoordinates);
        List<Coordinate> listOfUnconnectedCoordinates = Set.of(coordinates).stream()
                .filter(coordinate -> !setOfConnectedCoordinates.contains(coordinate)).distinct().collect(Collectors.toList());

        while (!listOfUnconnectedCoordinates.isEmpty()) {
            if (listOfUnconnectedCoordinates.size() > 2) {

            } else if (listOfUnconnectedCoordinates.size() > 1) {
                List<Coordinate> remainingCoordinates = new ArrayList<>(listOfUnconnectedCoordinates);
                Tour candidateTour1 = new Tour(listOfConnectedCoordinates);
                candidateTour1 = candidateTour1.newTourAfterInsertingPathAtCheapestPosition(remainingCoordinates);

                Tour candidateTour2 = new Tour(listOfConnectedCoordinates);
                candidateTour2 = candidateTour2.newTourAfterInsertingCoordinateAtCheapestPosition(remainingCoordinates.get(0));
                candidateTour2 = candidateTour2.newTourAfterInsertingCoordinateAtCheapestPosition(remainingCoordinates.get(1));

                if(candidateTour1.getDistance() < candidateTour2.getDistance()) {
                    listOfConnectedCoordinates = candidateTour1.getCoordinates();
                } else {
                    listOfConnectedCoordinates = candidateTour2.getCoordinates();
                }
                listOfUnconnectedCoordinates.remove(0);
                listOfUnconnectedCoordinates.remove(0);
            } else {
                Coordinate coordinateToMerge = listOfUnconnectedCoordinates.get(0);
                int position = Util.findCheapestPositionForGivenMerge(listOfConnectedCoordinates, coordinateToMerge);
                listOfConnectedCoordinates.add(position, coordinateToMerge);
                listOfUnconnectedCoordinates.remove(coordinateToMerge);
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
}
