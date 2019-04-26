package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;

class Euclidean2DTSPInstances {
    static final Euclidean2DTSPInstance INSTANCE_1 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 1),
                    new Coordinate(1, 0))
    );

    static final Euclidean2DTSPInstance INSTANCE_2 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(1, 0),
                    new Coordinate(2, 0),
                    new Coordinate(1, 2))
    );


    static final Euclidean2DTSPInstance INSTANCE_3 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(1, 3),
                    new Coordinate(1, 0),
                    new Coordinate(0, 2),
                    new Coordinate(2, 4),
                    new Coordinate(3, 3))
    );

    static final Euclidean2DTSPInstance INSTANCE_4 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(3, 0),
                    new Coordinate(4, 5),
                    new Coordinate(3, 3),
                    new Coordinate(0, 4),
                    new Coordinate(10, 4),
                    new Coordinate(4, 6))
    );

    static final Euclidean2DTSPInstance INSTANCE_5 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(3, 2),
                    new Coordinate(4, 2))
    );

    static final Euclidean2DTSPInstance INSTANCE_6 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(1, 2),
                    new Coordinate(6, 2))
    );

}
