package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolverA implements Euclidean2DTSPSolver {

    @Override
    public Tour compute(Euclidean2DTSPInstance instance) {
        Coordinate[] coordinates = instance.coordinates.toArray(new Coordinate[0]);

        ConvexHull convexHullNoGeom = new ConvexHull(coordinates, new GeometryFactory());
        Geometry convexHull = convexHullNoGeom.getConvexHull();
        List<Coordinate> connectedCoordinates = getListOfConnectedCoordinates(convexHull);
        List<Coordinate> tempConnectedCoordinates = Collections.unmodifiableList(connectedCoordinates);

        List<Coordinate> unconnectedCoordinates = new ArrayList<>(Arrays.asList(coordinates)).stream()
                .filter(coordinate -> !tempConnectedCoordinates.contains(coordinate)).distinct().collect(Collectors.toList());

        while (!unconnectedCoordinates.isEmpty()) {
            if (unconnectedCoordinates.size() > 2) {
                List<Coordinate> remainingCoordinates = new ArrayList<>(unconnectedCoordinates);
                Tour candidateTour2 = new Tour(connectedCoordinates);
                for (int i = 0; i < remainingCoordinates.size(); i++) {
                    candidateTour2 = candidateTour2.cheapestTourAfterInsertingCoordinate(remainingCoordinates.get(i));
                }
                IntStream.range(0, unconnectedCoordinates.size()).forEach(value -> unconnectedCoordinates.remove(0));
            } else if (unconnectedCoordinates.size() > 1) {
                List<Coordinate> remainingCoordinates = new ArrayList<>(unconnectedCoordinates);

                Tour candidateTour1 = new Tour(connectedCoordinates);
                candidateTour1 = candidateTour1.cheapestTourAfterInsertingPath(remainingCoordinates);

                Tour candidateTour2 = new Tour(connectedCoordinates);
                candidateTour2 = candidateTour2.cheapestTourAfterInsertingCoordinate(remainingCoordinates.get(0));
                candidateTour2 = candidateTour2.cheapestTourAfterInsertingCoordinate(remainingCoordinates.get(1));

                if (candidateTour1.getDistance() < candidateTour2.getDistance()) {
                    connectedCoordinates = candidateTour1.getCoordinates();
                } else {
                    connectedCoordinates = candidateTour2.getCoordinates();
                }
                IntStream.range(0, unconnectedCoordinates.size()).forEach(value -> unconnectedCoordinates.remove(0));
            } else {
                Coordinate coordinateToMerge = unconnectedCoordinates.get(0);
                int position = CheapestTourFinder.findCheapestPositionForGivenCoordinate(connectedCoordinates, coordinateToMerge);
                connectedCoordinates.add(position, coordinateToMerge);
                unconnectedCoordinates.remove(coordinateToMerge);
            }
        }

        return new Tour(connectedCoordinates);
    }

    private List<Coordinate> getListOfConnectedCoordinates(Geometry convexHull) {
        Coordinate[] coordinatesArray = convexHull.getCoordinates();
        List<Coordinate> coordinates = new ArrayList<>(Arrays.asList(coordinatesArray));
        int size = coordinates.size();
        int lastElement = size - 1;
        if (size > 1 && coordinates.get(0).equals2D(coordinates.get(lastElement))) {
            coordinates.remove(lastElement);
        }
        return coordinates;
    }
}
