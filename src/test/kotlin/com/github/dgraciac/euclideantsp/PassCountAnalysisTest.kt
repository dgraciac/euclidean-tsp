package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * E026 — Analisis del numero real de pasadas de 2-opt y or-opt.
 *
 * Objetivo: determinar si 2-opt y or-opt convergen en O(n) pasadas en la practica,
 * lo que permitiria reducir el safety limit de n^2 a c*n y ganar un grado en
 * la complejidad peor caso.
 *
 * Para cada instancia, ejecuta NN desde 10 puntos distintos y registra el numero
 * de pasadas de 2-opt y or-opt en cada ejecucion.
 */
internal class PassCountAnalysisTest {
    @ParameterizedTest
    @ArgumentsSource(TSPInstanceProvider::class)
    fun analyze_pass_counts(instance: Euclidean2DTSPInstance) {
        val n = instance.points.size
        val pointList = instance.points.toList()
        // Seleccionar 10 puntos de inicio equidistantes
        val starts = (0 until minOf(10, n)).map { pointList[it * n / minOf(10, n)] }

        val twoOptPasses = mutableListOf<Int>()
        val orOptPasses = mutableListOf<Int>()

        for (start in starts) {
            val nnTour = nearestNeighborFrom(instance.points, start)

            // 2-opt instrumentado
            val (_, twoOptCount) = twoOptCounting(nnTour)
            twoOptPasses.add(twoOptCount)

            // or-opt instrumentado (sobre tour ya optimizado con 2-opt)
            val (twoOptResult, _) = twoOptCounting(nnTour)
            val (_, orOptCount) = orOptCounting(twoOptResult)
            orOptPasses.add(orOptCount)
        }

        val avgTwoOpt = twoOptPasses.average()
        val maxTwoOpt = twoOptPasses.max()
        val avgOrOpt = orOptPasses.average()
        val maxOrOpt = orOptPasses.max()

        println(
            "Instance: ${instance.name} (n=$n) | " +
                "2-opt passes: avg=${"%.1f".format(avgTwoOpt)}, max=$maxTwoOpt, " +
                "ratio max/n=${"%.2f".format(maxTwoOpt.toDouble() / n)} | " +
                "or-opt passes: avg=${"%.1f".format(avgOrOpt)}, max=$maxOrOpt, " +
                "ratio max/n=${"%.2f".format(maxOrOpt.toDouble() / n)}",
        )
    }
}

/**
 * 2-opt que cuenta el numero de pasadas (cada pasada completa del tour).
 * @return par (tour mejorado, numero de pasadas)
 */
fun twoOptCounting(tourPoints: List<Point>): Pair<List<Point>, Int> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    var improved = true
    var passes = 0
    val maxPasses = n * n

    while (improved && passes < maxPasses) {
        improved = false
        passes++
        for (i in 0 until n - 1) {
            for (j in i + 2 until n) {
                if (i == 0 && j == n - 1) continue
                val a = points[i]
                val b = points[i + 1]
                val c = points[j]
                val d = points[(j + 1) % n]

                if (a.distance(b) + c.distance(d) > a.distance(c) + b.distance(d)) {
                    points.subList(i + 1, j + 1).reverse()
                    improved = true
                }
            }
        }
    }

    return Pair(points + points.first(), passes)
}

/**
 * Or-opt que cuenta el numero de pasadas.
 * @return par (tour mejorado, numero de pasadas)
 */
fun orOptCounting(tourPoints: List<Point>): Pair<List<Point>, Int> {
    val points = tourPoints.dropLast(1).toMutableList()
    var improved = true
    var passes = 0
    val maxPasses = points.size * points.size

    while (improved && passes < maxPasses) {
        improved = false
        passes++
        for (segSize in 1..minOf(3, points.size - 3)) {
            if (improved) break
            for (i in 0 until points.size - segSize) {
                if (tryRelocateCounting(points, i, segSize)) {
                    improved = true
                    break
                }
            }
        }
    }

    return Pair(points + points.first(), passes)
}

private fun tryRelocateCounting(
    points: MutableList<Point>,
    i: Int,
    segSize: Int,
): Boolean {
    val n = points.size
    val segEnd = i + segSize - 1
    val segFirst = points[i]
    val segLast = points[segEnd]
    val prevIdx = (i - 1 + n) % n
    val nextIdx = (i + segSize) % n

    val extractSaving =
        points[prevIdx].distance(segFirst) +
            segLast.distance(points[nextIdx]) -
            points[prevIdx].distance(points[nextIdx])

    val forbidden = mutableSetOf<Int>()
    forbidden.add(prevIdx)
    for (k in 0 until segSize) forbidden.add((i + k) % n)

    var bestGain = 1e-10
    var bestJ = -1

    for (j in 0 until n) {
        if (j in forbidden) continue
        val jNext = (j + 1) % n
        if (jNext in forbidden && jNext != nextIdx) continue

        val a = points[j]
        val b = points[jNext]
        val insertCost = a.distance(segFirst) + segLast.distance(b) - a.distance(b)
        val netGain = extractSaving - insertCost

        if (netGain > bestGain) {
            bestGain = netGain
            bestJ = j
        }
    }

    if (bestJ == -1) return false

    val segment = (0 until segSize).map { points[i + it] }
    for (k in segSize - 1 downTo 0) points.removeAt(i + k)
    val adjustedJ = if (bestJ > segEnd) bestJ - segSize else bestJ
    val insertAt = (adjustedJ + 1).coerceIn(0, points.size)
    points.addAll(insertAt, segment)
    return true
}
