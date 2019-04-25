package com.davidgracia.euclideantsp;

import java.util.Objects;

public class _2DPoint {
    public final double x;
    public final double y;

    public _2DPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        _2DPoint otherPoint = (_2DPoint) o;
        return x == otherPoint.x &&
                y == otherPoint.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
