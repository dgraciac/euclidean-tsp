package com.davidgracia.euclideantsp.solvers;

import com.github.dgraciac.euclideantsp.Euclidean2DTSPInstance;
import com.github.dgraciac.euclideantsp.Euclidean2DTSPSolver;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SolverA implements Euclidean2DTSPSolver {
    @NotNull
    @Override
    public Tour compute(@NotNull Euclidean2DTSPInstance instance) {
        return null;
    }
/*
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
                if(unconnectedCoordinates.size() == 3) {
                    List<Coordinate> remainingCoordinates = new ArrayList<>(unconnectedCoordinates);
                    Tour candidateTour = new Tour(connectedCoordinates);

                    while (!remainingCoordinates.isEmpty()) {
                        candidateTour = candidateTour.cheapestTourAfterInsertingBestCoordinateOf(remainingCoordinates);
                        remainingCoordinates.removeAll(candidateTour.getCoordinates());
                    }

                    connectedCoordinates = candidateTour.getCoordinates();
                    unconnectedCoordinates = new ArrayList<>();
                }
            } else if (unconnectedCoordinates.size() > 1) {
                List<Coordinate> remainingCoordinates = new ArrayList<>(unconnectedCoordinates);

                Tour candidateTour1 = new Tour(connectedCoordinates);
                candidateTour1 = candidateTour1.cheapestTourAfterInsertingPath(remainingCoordinates);

                Tour candidateTour2 = new Tour(connectedCoordinates);

                while (!remainingCoordinates.isEmpty()) {
                    candidateTour2 = candidateTour2.cheapestTourAfterInsertingBestCoordinateOf(remainingCoordinates);
                    remainingCoordinates.removeAll(candidateTour2.getCoordinates());
                }

                if (candidateTour1.getDistance() < candidateTour2.getDistance()) {
                    //TODO dead code?
                    connectedCoordinates = candidateTour1.getCoordinates();
                } else {
                    connectedCoordinates = candidateTour2.getCoordinates();
                }
                unconnectedCoordinates = new ArrayList<>();
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
    }*/
}
