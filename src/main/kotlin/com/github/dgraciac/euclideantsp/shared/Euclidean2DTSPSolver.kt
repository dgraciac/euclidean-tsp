package com.github.dgraciac.euclideantsp.shared

interface Euclidean2DTSPSolver {
    fun compute(instance: Euclidean2DTSPInstance): Tour
}