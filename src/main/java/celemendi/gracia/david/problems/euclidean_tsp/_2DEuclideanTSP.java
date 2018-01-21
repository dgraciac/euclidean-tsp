package celemendi.gracia.david.problems.euclidean_tsp;

import celemendi.gracia.david.problems.ComputationalProblem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;
import java.util.List;

public class _2DEuclideanTSP extends ComputationalProblem {

    private List<Point> pointList;

    public _2DEuclideanTSP(Coordinate[] coordinateArray) {
        super();
        this.description = "Given a collection of 2D points, what is the shortest possible route that visits each point " +
                "and returns to the origin point?";
        pointList = new ArrayList<>();
        for (Coordinate coordinate: coordinateArray) {
            Coordinate[] coordinateArray2 = {coordinate};
            CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinateArray2,2);
            Point point = new Point(coordinateSequence, new GeometryFactory());
            pointList.add(point);
        }
    }

    public List<Point> getPointList() {
        return pointList;
    }
}
