package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp.Euclidean2DTSPInstance;

@FunctionalInterface
public interface Euclidean2DTSPSolver {
    Tour compute(Euclidean2DTSPInstance instance);
}
