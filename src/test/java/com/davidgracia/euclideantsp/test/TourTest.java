package com.davidgracia.euclideantsp.test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TourTest {
    @Test void
    equality() {
        _2DPoint a = new _2DPoint(3, 6);
        _2DPoint b = new _2DPoint(5, 9);
        _2DPoint c = new _2DPoint(7, 1);
        Tour tour1 = new Tour(a, b, c);
        Tour tour2 = new Tour(b, c, a);

        assertThat(tour1).isEqualTo(tour2);
    }
}