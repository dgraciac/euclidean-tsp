package com.davidgracia.euclideantsp.solvers;

import com.github.dgraciac.euclideantsp.Euclidean2DTSPInstance;
import com.github.dgraciac.euclideantsp.Euclidean2DTSPSolver;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;

public class BruteForce implements Euclidean2DTSPSolver {
    @NotNull
    @Override
    public Tour compute(@NotNull Euclidean2DTSPInstance instance) {
        return null;
    }
/*    public Tour compute(Euclidean2DTSPInstance instance) {
        Coordinate[] coordinates = new Coordinate[instance.coordinates.size()];
        instance.coordinates.toArray(coordinates);

        int[] indexes = new int[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            indexes[i] = 0;
        }

        Tour bestTour = new Tour(coordinates);

        int i = 0;
        while (i < coordinates.length) {
            if (indexes[i] < i) {
                swap(coordinates, i % 2 == 0 ? 0 : indexes[i], i);
                Tour tour = new Tour(coordinates);
                if (tour.getDistance() < bestTour.getDistance()) {
                    bestTour = tour;
                }
                indexes[i]++;
                i = 0;
            } else {
                indexes[i] = 0;
                i++;
            }
        }
        return bestTour;
    }

    private void swap(Coordinate[] input, int a, int b) {
        Coordinate tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }*/
}
