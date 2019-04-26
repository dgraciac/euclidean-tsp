package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;

import java.util.List;

public class Util {
    public static int findCheapestPositionForGivenMerge(List<Coordinate> coordinates, Coordinate coordinate) {
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
