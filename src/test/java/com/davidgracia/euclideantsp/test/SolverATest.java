package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;
import com.davidgracia.euclideantsp.solvers.BruteForce;
import com.davidgracia.euclideantsp.solvers.SolverA;
import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class SolverATest {
    private SolverA algorithm;

    private BruteForce bruteForce;

    @BeforeEach
    void setUp() {
        algorithm = new SolverA();
        bruteForce = new BruteForce();
    }

    @ParameterizedTest
    @ArgumentsSource(TSPInstancesProvider.class) void
    solve_instances(Euclidean2DTSPInstance instance) {
        Tour tour = algorithm.compute(instance);
        Tour optimalTour = bruteForce.compute(instance);
        System.out.println("Brute force solution: " + optimalTour);
        System.out.println("Efficient solution  : " + tour);
        System.out.println("==============================================");
        assertThat(tour.equals(optimalTour) || tour.getDistance() == optimalTour.getDistance()).isTrue();
    }
}