package celemendi.gracia.david.problems.euclidean_tsp.solvers;

import celemendi.gracia.david.problems.euclidean_tsp._2DEuclideanTSP;
import celemendi.gracia.david.problems.euclidean_tsp._2DEuclideanTSPSolver;
import org.locationtech.jts.geom.Point;

import java.util.List;

public class BruteForce implements _2DEuclideanTSPSolver {

    private Double[][] distances;

    private void calculateDistances(List<Point> pointList) {
        Integer pointListSize = pointList.size();
        distances = new Double[pointListSize][pointListSize];
        for (int i = 0; i < pointListSize; i++) {
            for(int j = i; j < pointListSize; j++) {
                if(i == j) {
                    distances[i][j] = .0;
                } else {
                    Point pointA = pointList.get(i);
                    Point pointB = pointList.get(j);
                    distances[j][i] = distances[i][j] = pointA.distance(pointB);
                }
            }
        }
    }

    @Override
    public void compute(_2DEuclideanTSP computationalProblem) {
        List<Point> pointList = computationalProblem.getPointList();
        calculateDistances(pointList);
        celemendi.gracia.david.utilities.Util.permute(pointList);
        //TODO use Util.permute.
    }
}
