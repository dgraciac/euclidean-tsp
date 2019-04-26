package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;
import com.davidgracia.euclideantsp.solvers.BruteForce;
import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

class BruteForceTest {

    private BruteForce algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new BruteForce();
    }

    @ParameterizedTest
    @ArgumentsSource(TSPInstancesProvider.class) void
    solve_instances(Euclidean2DTSPInstance instance) {
        Tour optimalTour = algorithm.compute(instance);
        System.out.println("Brute force solution: " + optimalTour);
        System.out.println("==============================================");
    }

    private void printSolutionInJava(Tour tour) {
        List<Coordinate> points = tour.getCoordinates();

        for (int i = 0; i < points.size() - 1; i++) {
            Coordinate point = points.get(i);
            System.out.println("new " + Coordinate.class.getSimpleName() + "(" + point.x + ", " + point.y + "),");
        }
        Coordinate point = points.get(points.size() - 1);
        System.out.println("new " + Coordinate.class.getSimpleName() + "(" + point.x + ", " + point.y + ")");
    }
}