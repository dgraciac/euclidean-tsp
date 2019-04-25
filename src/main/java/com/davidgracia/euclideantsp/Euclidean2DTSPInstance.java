package com.davidgracia.euclideantsp;

import org.locationtech.jts.geom.Coordinate;

import java.util.Collections;
import java.util.List;

public class Euclidean2DTSPInstance {
    public final List<Coordinate> coordinates;

    public Euclidean2DTSPInstance(List<Coordinate> coordinates) {
        this.coordinates = Collections.unmodifiableList(coordinates);
    }
}
