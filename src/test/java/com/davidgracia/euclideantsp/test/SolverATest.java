package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp._2DEuclideanTSPInstances;
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
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_1);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance2() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_2);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance3() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_3);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance4() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_4_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_4);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance5() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_5_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_5);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

    @Test void
    instance6() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_6_SOLUTION;
        Tour tour = algorithm.compute(_2DEuclideanTSPInstances.INSTANCE_6);
        System.out.println(tour);
        assertThat(tour).isEqualTo(optimalTour);
    }

}