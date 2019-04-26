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

    public static final double INSTANCE_1_SOLUTION = 3.414213562373095;

    public static final Euclidean2DTSPInstance INSTANCE_2 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(1, 0),
                    new Coordinate(2, 0),
                    new Coordinate(1, 2))
    );

    public static final double INSTANCE_2_SOLUTION = 6.47213595499958;


    public static final Euclidean2DTSPInstance INSTANCE_3 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(1, 3),
                    new Coordinate(1, 0),
                    new Coordinate(0, 2),
                    new Coordinate(2, 4),
                    new Coordinate(3, 3))
    );

    public static final double INSTANCE_3_SOLUTION = 10.084259940083063;

    public static final Euclidean2DTSPInstance INSTANCE_4 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(3, 0),
                    new Coordinate(4, 5),
                    new Coordinate(3, 3),
                    new Coordinate(0, 4),
                    new Coordinate(10, 4),
                    new Coordinate(4, 6))
    );

    public static final double INSTANCE_4_SOLUTION = 25.672196354421345;

    public static final Euclidean2DTSPInstance INSTANCE_5 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(3, 2),
                    new Coordinate(4, 2))
    );

    public static final double INSTANCE_5_SOLUTION = 23.211102550927976;

    public static final Euclidean2DTSPInstance INSTANCE_6 = new Euclidean2DTSPInstance(
            Arrays.asList(
                    new Coordinate(0, 0),
                    new Coordinate(0, 4),
                    new Coordinate(7, 0),
                    new Coordinate(7, 4),
                    new Coordinate(1, 2),
                    new Coordinate(6, 2))
    );

    public static final double INSTANCE_6_SOLUTION = 22.94427190999916;

}
