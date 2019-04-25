package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp._2DEuclideanTSPInstance;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolverA implements _2DEuclideanTSPSolver {
    @Override
    public Tour compute(_2DEuclideanTSPInstance instance) {
        Coordinate[] points = instance.points.stream()
                .map(PointMapper::toPoint)
                .map(Point::getCoordinate)
                .toArray(Coordinate[]::new);

        ConvexHull convexHullNoGeom = new ConvexHull(points, new GeometryFactory());
        Geometry convexHull = convexHullNoGeom.getConvexHull();
        List<Coordinate> listOfConnectedPoints = Arrays.asList(convexHull.getCoordinates());

        Set<Coordinate> setOfConnectedPoints = Set.copyOf(listOfConnectedPoints);
        Set<Coordinate> setOfUnconnectedPoints = Set.of(points).stream().filter(coordinate -> !setOfConnectedPoints.contains(coordinate)).collect(Collectors.toSet());
        List<Coordinate> listOfUnconnectedPoints = List.copyOf(setOfUnconnectedPoints);

        while (!listOfUnconnectedPoints.isEmpty()) {
            if(listOfConnectedPoints.size() > 2) {

            } else {

            }
        }

        //TODO listOfConnectedPoints to Tour
        // return tour
        return null;
    }
}
