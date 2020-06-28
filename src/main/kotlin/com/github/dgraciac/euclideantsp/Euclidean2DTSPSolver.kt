package com.github.dgraciac.euclideantsp

import com.davidgracia.euclideantsp.solvers.Tour

interface Euclidean2DTSPSolver {
    fun compute(instance: Euclidean2DTSPInstance): Tour
}