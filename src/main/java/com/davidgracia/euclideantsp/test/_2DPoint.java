package com.davidgracia.euclideantsp.test;

import java.util.Objects;

public class _2DPoint {
    public final int x;
    public final int y;

    public _2DPoint(int x, int y) {
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
}
