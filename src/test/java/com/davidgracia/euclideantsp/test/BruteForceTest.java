package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.BruteForce;
import com.davidgracia.euclideantsp.Tour;
import com.davidgracia.euclideantsp._2DEuclideanTSPInstances;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BruteForceTest {

    @Test void
    instance1() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = new BruteForce().compute(_2DEuclideanTSPInstances.INSTANCE_1);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance2() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = new BruteForce().compute(_2DEuclideanTSPInstances.INSTANCE_2);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance3() {
        Tour optimalTour = _2DEuclideanTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = new BruteForce().compute(_2DEuclideanTSPInstances.INSTANCE_3);
        assertThat(tour).isEqualTo(optimalTour);
        System.out.println(tour);
    }

    @Test void
    instance4() {
        Tour tour = new BruteForce().compute(_2DEuclideanTSPInstances.INSTANCE_4);
        System.out.println(tour);
    }
}