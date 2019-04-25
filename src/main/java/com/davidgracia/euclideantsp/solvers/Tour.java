package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp._2DPoint;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.stream.Collectors;

public class Tour {
    private final List<_2DPoint> points;
    private static final double NOT_CALCULATED_YET = -1;
    private double distance;

    public Tour(_2DPoint firstPoint, _2DPoint... otherPoints) {
        List<_2DPoint> tempPoints = new ArrayList<>();
        tempPoints.add(firstPoint);
        tempPoints.addAll(Arrays.asList(otherPoints));
        points = Collections.unmodifiableList(tempPoints);
        distance = NOT_CALCULATED_YET;
    }

    public Tour(_2DPoint[] points) {
        this.points = List.of(points);
        distance = NOT_CALCULATED_YET;
    }

    public double getDistance() {
        return distance == NOT_CALCULATED_YET ? distance = calculateDistance(this.points) : distance;
    }

    private double calculateDistance(List<_2DPoint> _2DPoints) {
        double distance = 0;
        List<Point> points = _2DPoints.stream().map(PointMapper::toPoint).collect(Collectors.toUnmodifiableList());
        for (int i = 0; i < points.size() - 1; i++) {
            distance += points.get(i).distance(points.get(i + 1));
        }
        distance += points.get(points.size() - 1).distance(points.get(0));
        return distance;
    }

    public List<_2DPoint> getPoints() {
        return points;
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
        while (i < points.size() && !areEquals) {
            Collections.rotate(tempPoints, 1);
            areEquals = Objects.equals(points, tempPoints);
            Collections.reverse(tempPoints);
            areEquals = areEquals || Objects.equals(points, tempPoints);
            Collections.reverse(tempPoints);
            i++;
        }
        return areEquals;
    }

    @Override
    public int hashCode() {
        //TODO
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "Tour{" + points + ", d=" + distance + "}";
    }
}
