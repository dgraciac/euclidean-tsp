package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstances;
import com.davidgracia.euclideantsp.solvers.BruteForce;
import com.davidgracia.euclideantsp.solvers.SolverA;
import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolverATest {
    private SolverA algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new SolverA();
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
        Tour bruteForceSolution = new BruteForce().compute(Euclidean2DTSPInstances.INSTANCE_4);
        System.out.println("Brute force: " + bruteForceSolution);
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

}