package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstances;
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

    @Test
    void
    instance1() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_1);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance2() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_2);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance3() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_3);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance4() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_4_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_4);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance5() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_5_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_5);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance6() {
        Tour optimalTour = Euclidean2DTSPInstances.INSTANCE_6_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_6);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

}