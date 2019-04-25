package com.davidgracia.euclideantsp;

import com.davidgracia.euclideantsp.solvers.Tour;
import org.locationtech.jts.geom.Coordinate;

import java.util.Arrays;

public class Euclidean2DTSPInstances {
    public static final Euclidean2DTSPInstance INSTANCE_1 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 1),
                    new Coordinate(1, 0))
    );

    public static final Tour INSTANCE_1_SOLUTION = new Tour(
            new Coordinate(0, 1),
            new Coordinate(0, 0),
            new Coordinate(1, 0));

    public static final Euclidean2DTSPInstance INSTANCE_2 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(1, 0),
                    new Coordinate(2, 0),
                    new Coordinate(1, 2))
    );

    public static final Tour INSTANCE_2_SOLUTION = new Tour(
            new Coordinate(0, 0),
            new Coordinate(1, 0),
            new Coordinate(2, 0),
            new Coordinate(1, 2));


    public static final Euclidean2DTSPInstance INSTANCE_3 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(1, 3),
                    new Coordinate(1, 0),
                    new Coordinate(0, 2),
                    new Coordinate(2, 4),
                    new Coordinate(3, 3))
    );

    public static final Tour INSTANCE_3_SOLUTION = new Tour(
            new Coordinate(1, 0),
            new Coordinate(0, 2),
            new Coordinate(1, 3),
            new Coordinate(2, 4),
            new Coordinate(3, 3));

    public static final Euclidean2DTSPInstance INSTANCE_4 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(3, 0),
                    new Coordinate(4, 5),
                    new Coordinate(3, 3),
                    new Coordinate(0, 4),
                    new Coordinate(10, 4),
                    new Coordinate(4, 6))
    );

    public static final Tour INSTANCE_4_SOLUTION = new Tour(
            new Coordinate(0.0, 4.0),
            new Coordinate(4.0, 5.0),
            new Coordinate(4.0, 6.0),
            new Coordinate(10.0, 4.0),
            new Coordinate(3.0, 0.0),
            new Coordinate(3.0, 3.0)
    );

    public static final Euclidean2DTSPInstance INSTANCE_5 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(3, 2),
                    new Coordinate(4, 2))
    );

    public static final Tour INSTANCE_5_SOLUTION = new Tour(
            new Coordinate(7.0, 4.0),
            new Coordinate(7.0, 0.0),
            new Coordinate(0.0, 0.0),
            new Coordinate(0.0, 4.0),
            new Coordinate(3.0, 2.0),
            new Coordinate(4.0, 2.0)
    );

    public static final Euclidean2DTSPInstance INSTANCE_6 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(1, 2),
                    new Coordinate(6, 2))
    );

    public static final Tour INSTANCE_6_SOLUTION = new Tour(
            new Coordinate(7.0, 4.0),
            new Coordinate(0.0, 4.0),
            new Coordinate(1.0, 2.0),
            new Coordinate(0.0, 0.0),
            new Coordinate(7.0, 0.0),
            new Coordinate(6.0, 2.0)
    );

}
