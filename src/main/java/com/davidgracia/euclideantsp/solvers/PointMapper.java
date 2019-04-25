package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp._2DPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class PointMapper {
    public static Point toPoint(_2DPoint _2DPoint) {
        Coordinate coordinate = new CoordinateXY(_2DPoint.x, _2DPoint.y);
        return new GeometryFactory().createPoint(coordinate);
    }
}
