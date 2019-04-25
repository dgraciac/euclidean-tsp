package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.solvers.Tour;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.assertj.core.api.Assertions.assertThat;

class TourTest {
    @Test void
    equality1() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(b, c, a);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality2() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(c, a, b);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality3() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(a, b, c);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality4() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(c, b, a);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality5() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(b, a, c);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality6() {
        Coordinate a = new Coordinate(3, 6);
        Coordinate b = new Coordinate(5, 9);
        Coordinate c = new Coordinate(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(a, c, b);

        assertThat(tour1).isEqualTo(tour2);
    }

}