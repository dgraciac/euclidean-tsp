package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.BruteForce;
import com.davidgracia.euclideantsp.Tour;
import com.davidgracia.euclideantsp._2DEuclideanTSPInstances;
import com.davidgracia.euclideantsp._2DPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_1);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance2() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_2);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance3() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_3);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance4() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_4_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_4);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    private void printSolutionInJava(Tour tour) {
        List<_2DPoint> points = tour.getPoints();

        for (int i = 0; i < points.size() - 1; i++) {
            _2DPoint point = points.get(i);
            System.out.println("new " + _2DPoint.class.getSimpleName() + "(" + point.x + ", " + point.y + "),");
        }
        _2DPoint point = points.get(points.size() - 1);
        System.out.println("new " + _2DPoint.class.getSimpleName() + "(" + point.x + ", " + point.y + ")");
    }
}