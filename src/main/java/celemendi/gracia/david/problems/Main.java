package celemendi.gracia.david.problems;

import celemendi.gracia.david.problems.euclidean_tsp._2DEuclideanTSP;
import celemendi.gracia.david.problems.euclidean_tsp.solvers.BruteForce;
import org.locationtech.jts.geom.Coordinate;

public class Main {

    public static void main(String[] args) {
        Coordinate[] coordinateArray = {new Coordinate(1,1), new Coordinate(0,0), new Coordinate(1,2)};
        _2DEuclideanTSP _2DEuclideanTSP = new _2DEuclideanTSP(coordinateArray);
        BruteForce bruteForce = new BruteForce();
        bruteForce.compute(_2DEuclideanTSP);
    }
}
