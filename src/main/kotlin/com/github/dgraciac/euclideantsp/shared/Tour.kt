package com.github.dgraciac.euclideantsp.shared

import com.github.dgraciac.euclideantsp.length

data class Tour(val points: List<Point>) {
    val length: Double = points.length()
}
