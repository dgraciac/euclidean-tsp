package com.davidgracia.euclideantsp;

public class BruteForce extends _2DEuclideanTSPSolver {

    public Tour compute(_2DEuclideanTSPInstance instance){

        _2DPoint[] points = new _2DPoint[instance.points.size()];
        instance.points.toArray(points);

        int[] indexes = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            indexes[i] = 0;
        }

        Tour bestTour = new Tour(points);

        int i = 0;
        while (i < points.length) {
            if (indexes[i] < i) {
                swap(points, i % 2 == 0 ? 0 : indexes[i], i);
                Tour tour = new Tour(points);
                if(tour.getDistance() < bestTour.getDistance()) bestTour = tour;
                indexes[i]++;
                i = 0;
            } else {
                indexes[i] = 0;
                i++;
            }
        }
        return bestTour;
    }

    private void swap(_2DPoint[] input, int a, int b) {
        _2DPoint tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }
}
