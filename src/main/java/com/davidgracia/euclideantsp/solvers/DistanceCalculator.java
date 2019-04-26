package com.davidgracia.euclideantsp.solvers;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.List;

class DistanceCalculator {
    static double calculateTourLength(List<Coordinate> coordinates) {
        GeometryFactory geometryFactory = new GeometryFactory();
        LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
        return lineString.getLength() + lineString.getEndPoint().distance(lineString.getStartPoint());
    }
}
