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

}