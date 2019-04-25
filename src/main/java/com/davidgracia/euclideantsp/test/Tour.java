package com.davidgracia.euclideantsp.test;

import java.util.*;

public class Tour {
    private List<_2DPoint> points;

    public Tour(_2DPoint firstPoint, _2DPoint... otherPoints) {
        points = new ArrayList<>();
        points.add(firstPoint);
        points.addAll(Arrays.asList(otherPoints));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tour otherTour = (Tour) o;

        boolean areEquals = false;
        int i = 0;
        List<_2DPoint> tempPoints = Arrays.asList(new _2DPoint[points.size()]);
        Collections.copy(tempPoints, otherTour.points);
        while (i < points.size() && !areEquals){
            Collections.rotate(tempPoints, 1);
            areEquals = Objects.equals(points, tempPoints);
            i++;
        }
        return areEquals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }
}
