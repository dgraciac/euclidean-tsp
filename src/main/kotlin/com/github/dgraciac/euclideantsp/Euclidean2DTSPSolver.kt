package com.github.dgraciac.euclideantsp

interface Euclidean2DTSPSolver {
    fun compute(instance: Euclidean2DTSPInstance): Tour
}