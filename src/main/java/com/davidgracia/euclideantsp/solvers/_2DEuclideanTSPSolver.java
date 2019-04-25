package com.davidgracia.euclideantsp.solvers;

import com.davidgracia.euclideantsp._2DEuclideanTSPInstance;

@FunctionalInterface
public interface _2DEuclideanTSPSolver {
    Tour compute(_2DEuclideanTSPInstance instance);
}
