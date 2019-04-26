package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstances;
import com.davidgracia.euclideantsp.solvers.BruteForce;
import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BruteForceTest {

    private BruteForce algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new BruteForce();
    }

    @Test void
    instance1() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_1);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test void
    instance2() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_2);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test void
    instance3() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_3);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test void
    instance4() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_4_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_4);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test void
    instance5() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_5_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_5);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test void
    instance6() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_6_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_6);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
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