package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

class Util {
    static int findCheapestPositionForGivenCoordinate(List<Coordinate> coordinates, Coordinate coordinate) {
        double minimumDistance = Double.POSITIVE_INFINITY;
        int position = -1;
        for (int i = 0; i < coordinates.size(); i++) {
            List<Coordinate> candidateCoordinates = new ArrayList<>(coordinates);
            candidateCoordinates.add(i, coordinate);
            double distance = DistanceCalculator.calculateTourLength(candidateCoordinates);
            if (distance < minimumDistance) {
                minimumDistance = distance;
                position = i;
            }
        }
        return position;
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
    }
}
