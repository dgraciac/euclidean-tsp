package com.davidgracia.euclideantsp;

import com.davidgracia.euclideantsp.solvers.Tour;

import java.util.Arrays;

public class _2DEuclideanTSPInstances {
    public static final _2DEuclideanTSPInstance INSTANCE_1 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(0, 0),
                    new _2DPoint(0, 1),
                    new _2DPoint(1, 0))
    );

    public static final Tour INSTANCE_1_SOLUTION = new Tour(
            new _2DPoint(0, 1),
            new _2DPoint(0, 0),
            new _2DPoint(1, 0));

    public static final _2DEuclideanTSPInstance INSTANCE_2 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(0, 0),
                    new _2DPoint(1, 0),
                    new _2DPoint(2, 0),
                    new _2DPoint(1, 2))
    );

    public static final Tour INSTANCE_2_SOLUTION = new Tour(
            new _2DPoint(0, 0),
            new _2DPoint(1, 0),
            new _2DPoint(2, 0),
            new _2DPoint(1, 2));


    public static final _2DEuclideanTSPInstance INSTANCE_3 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(1, 3),
                    new _2DPoint(1, 0),
                    new _2DPoint(0, 2),
                    new _2DPoint(2, 4),
                    new _2DPoint(3, 3))
    );

    public static final Tour INSTANCE_3_SOLUTION = new Tour(
            new _2DPoint(1, 0),
            new _2DPoint(0, 2),
            new _2DPoint(1, 3),
            new _2DPoint(2, 4),
            new _2DPoint(3, 3));

    public static final _2DEuclideanTSPInstance INSTANCE_4 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(3, 0),
                    new _2DPoint(4, 5),
                    new _2DPoint(3, 3),
                    new _2DPoint(0, 4),
                    new _2DPoint(10, 4),
                    new _2DPoint(4, 6))
    );

    public static final Tour INSTANCE_4_SOLUTION = new Tour(
            new _2DPoint(0.0, 4.0),
            new _2DPoint(4.0, 5.0),
            new _2DPoint(4.0, 6.0),
            new _2DPoint(10.0, 4.0),
            new _2DPoint(3.0, 0.0),
            new _2DPoint(3.0, 3.0)
    );

    public static final _2DEuclideanTSPInstance INSTANCE_5 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(0, 0),
                    new _2DPoint(0, 4),
                    new _2DPoint(7, 0),
                    new _2DPoint(7, 4),
                    new _2DPoint(3, 2),
                    new _2DPoint(4, 2))
    );

    public static final Tour INSTANCE_5_SOLUTION = new Tour(
            new _2DPoint(7.0, 4.0),
            new _2DPoint(7.0, 0.0),
            new _2DPoint(0.0, 0.0),
            new _2DPoint(0.0, 4.0),
            new _2DPoint(3.0, 2.0),
            new _2DPoint(4.0, 2.0)
    );

    public static final _2DEuclideanTSPInstance INSTANCE_6 = new _2DEuclideanTSPInstance(
            Arrays.asList(
                    new _2DPoint(0, 0),
                    new _2DPoint(0, 4),
                    new _2DPoint(7, 0),
                    new _2DPoint(7, 4),
                    new _2DPoint(1, 2),
                    new _2DPoint(6, 2))
    );

    public static final Tour INSTANCE_6_SOLUTION = new Tour(
            new _2DPoint(7.0, 4.0),
            new _2DPoint(0.0, 4.0),
            new _2DPoint(1.0, 2.0),
            new _2DPoint(0.0, 0.0),
            new _2DPoint(7.0, 0.0),
            new _2DPoint(6.0, 2.0)
    );

}
