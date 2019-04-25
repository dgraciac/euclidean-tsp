package com.davidgracia.euclideantsp.test;

import com.davidgracia.euclideantsp.solvers.Tour;
import com.davidgracia.euclideantsp._2DPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TourTest {
    @Test void
    equality1() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(b, c, a);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality2() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(c, a, b);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality3() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(a, b, c);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality4() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(c, b, a);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality5() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(b, a, c);

        assertThat(tour1).isEqualTo(tour2);
    }

    @Test void
    equality6() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(a, c, b);

        assertThat(tour1).isEqualTo(tour2);
    }

}