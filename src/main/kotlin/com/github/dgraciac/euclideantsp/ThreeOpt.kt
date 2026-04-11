package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Aplica busqueda local 3-opt sobre un tour.
 * Para cada triplete de aristas (i,i+1), (j,j+1), (k,k+1), prueba todas las
 * reconexiones validas que mejoran el tour. 3-opt puede resolver estructuras
 * que 2-opt no puede (como "double bridges").
 *
 * Solo implementa los movimientos 3-opt "puros" (no reducibles a 2-opt),
 * ya que se asume que 2-opt ya se ejecuto previamente.
 *
 * @param tourPoints lista de puntos del tour (cerrado: primero == ultimo)
 * @return tour mejorado (cerrado: primero == ultimo)
 * Complejidad: O(n^3) por pasada, O(1) pasadas tipicas tras 2-opt+or-opt
 */
fun threeOpt(tourPoints: List<Point>): List<Point> {
    val points = tourPoints.dropLast(1).toMutableList()
    val n = points.size
    if (n < 6) return tourPoints

    var improved = true
    while (improved) {
        improved = false
        for (i in 0 until n - 4) {
            if (improved) break
            for (j in i + 2 until n - 2) {
                if (improved) break
                for (k in j + 2 until n) {
                    if (i == 0 && k == n - 1) continue

                    val gain = bestThreeOptMove(points, n, i, j, k)
                    if (gain > 1e-10) {
                        applyBestThreeOptMove(points, n, i, j, k)
                        improved = true
                        break
                    }
                }
            }
        }
    }

    return points + points.first()
}

/**
 * Calcula la ganancia del mejor movimiento 3-opt puro para las posiciones i, j, k.
 * Un movimiento 3-opt puro reconecta los 3 segmentos de forma que no se puede
 * descomponer en movimientos 2-opt.
 *
 * Segmentos: A = [i+1..j], B = [j+1..k], C = [k+1..i] (circular)
 * Aristas eliminadas: (i, i+1), (j, j+1), (k, k+1)
 *
 * Complejidad: O(1)
 */
private fun bestThreeOptMove(
    points: List<Point>,
    n: Int,
    i: Int,
    j: Int,
    k: Int,
): Double {
    val p0 = points[i]
    val p1 = points[i + 1]
    val p2 = points[j]
    val p3 = points[j + 1]
    val p4 = points[k]
    val p5 = points[(k + 1) % n]

    val d01 = p0.distance(p1)
    val d23 = p2.distance(p3)
    val d45 = p4.distance(p5)
    val currentCost = d01 + d23 + d45

    // Movimiento 3-opt puro tipo 1: A, reverso(B), C -> conectar (p0,p3), (p2,p5), (p4,p1)
    // Segmentos: [..i] -> [j+1..k] -> [j..i+1] -> [k+1..]
    val newCost1 = p0.distance(p3) + p4.distance(p1) + p2.distance(p5)

    // Movimiento 3-opt puro tipo 2: reverso(A), B, reverso(C) -> (p0,p4), (p3,p1), (p2,p5)
    // Segmentos: [..i] -> [k..j+1] -> [i+1..j] -> [k+1..]
    val newCost2 = p0.distance(p4) + p3.distance(p1) + p2.distance(p5)

    val bestNewCost = minOf(newCost1, newCost2)
    return currentCost - bestNewCost
}

/**
 * Aplica el mejor movimiento 3-opt puro encontrado.
 * Complejidad: O(n) para la reorganizacion de segmentos
 */
private fun applyBestThreeOptMove(
    points: MutableList<Point>,
    n: Int,
    i: Int,
    j: Int,
    k: Int,
) {
    val p0 = points[i]
    val p1 = points[i + 1]
    val p2 = points[j]
    val p3 = points[j + 1]
    val p4 = points[k]
    val p5 = points[(k + 1) % n]

    val d01 = p0.distance(p1)
    val d23 = p2.distance(p3)
    val d45 = p4.distance(p5)
    val currentCost = d01 + d23 + d45

    val newCost1 = p0.distance(p3) + p4.distance(p1) + p2.distance(p5)
    val newCost2 = p0.distance(p4) + p3.distance(p1) + p2.distance(p5)

    // Segmentos originales: A = [i+1..j], B = [j+1..k]
    val segA = (i + 1..j).map { points[it] }
    val segB = (j + 1..k).map { points[it] }

    if (newCost1 <= newCost2 && currentCost - newCost1 > 1e-10) {
        // Tipo 1: [..i] -> B -> reverso(A) -> [k+1..]
        var idx = i + 1
        for (p in segB) points[idx++] = p
        for (p in segA.reversed()) points[idx++] = p
    } else if (currentCost - newCost2 > 1e-10) {
        // Tipo 2: [..i] -> reverso(B) -> A -> [k+1..]
        var idx = i + 1
        for (p in segB.reversed()) points[idx++] = p
        for (p in segA) points[idx++] = p
    }
}
