package com.davidgracia.euclideantsp;

import java.util.Collections;
import java.util.List;

public class _2DEuclideanTSPInstance {
    public final List<_2DPoint> points;

    public _2DEuclideanTSPInstance(List<_2DPoint> points) {
        this.points = Collections.unmodifiableList(points);
    }
}
