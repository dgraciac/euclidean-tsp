package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstances;
import com.davidgracia.euclideantsp.solvers.BruteForce;
import com.davidgracia.euclideantsp.solvers.SolverA;
import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

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
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_1_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_1);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    void
    instance2() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_2_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_2);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    void
    instance3() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_3_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_3);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    void
    instance4() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_4_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_4);
        Tour bruteForceSolution = new BruteForce().compute(Euclidean2DTSPInstances.INSTANCE_4);
        System.out.println("Brute force: " + bruteForceSolution);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    void
    instance5() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_5_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_5);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    void
    instance6() {
        double optimalTour = Euclidean2DTSPInstances.INSTANCE_6_SOLUTION;
        Tour tour = algorithm.compute(Euclidean2DTSPInstances.INSTANCE_6);
        System.out.println("Tour: " + tour);
        assertThat(tour.getDistance()).isEqualTo(optimalTour);
    }

    @Test
    @Disabled
    void
    sadsad() {
        /*Brute force: Tour{(0.0,4.0)(4.0,5.0)(4.0,6.0)(10.0,4.0)(3.0,0.0)(3.0,3.0), d=25.672196354421345}
        Tour: Tour{(3.0,0.0)(3.0,3.0)(0.0,4.0)(4.0,5.0)(4.0,6.0)(10.0,4.0), d=25.67219635442135}*/
        Tour tour1 = new Tour(
                new Coordinate(0, 4),
                new Coordinate(4, 5),
                new Coordinate(4, 6),
                new Coordinate(10, 4),
                new Coordinate(3, 0),
                new Coordinate(3, 3)
        );

        Tour tour2 = new Tour(
                new Coordinate(3, 0),
                new Coordinate(3, 3),
                new Coordinate(0, 4),
                new Coordinate(4, 5),
                new Coordinate(4, 6),
                new Coordinate(10, 4)
        );

        System.out.println(tour1);
        System.out.println(tour2);
    }

}