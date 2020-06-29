package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

class CheapestTourFinder {
/*    static int findCheapestPositionForGivenCoordinate(List<Coordinate> coordinates, Coordinate coordinate) {
        List<Coordinate> coordinatesToAdd = List.of(coordinate);
        return findCheapestPositionForGivenPathDirectionSensitive(coordinates, coordinatesToAdd);
    }

    static int findCheapestPositionForGivenPathDirectionSensitive(List<Coordinate> coordinates, List<Coordinate> coordinatesToAdd) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        int position = -1;
        for (int i = 0; i < coordinates.size(); i++) {
            List<Coordinate> candidateCoordinates = new ArrayList<>(coordinates);
            candidateCoordinates.addAll(i, coordinatesToAdd);
            double distance = DistanceCalculator.calculateTourLength(candidateCoordinates);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                position = i;
            }
        }
        return position;
    }*/
}
