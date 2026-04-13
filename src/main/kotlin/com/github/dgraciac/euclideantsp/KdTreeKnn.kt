package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Point
import java.util.PriorityQueue

/**
 * KD-tree optimizado para K-nearest neighbors en 2D.
 *
 * Construye un arbol KD estatico sobre un conjunto de puntos 2D y permite
 * consultas eficientes de los K vecinos mas cercanos a un punto dado.
 *
 * @param points puntos a indexar
 *
 * Complejidad de construccion: O(n log n)
 * Complejidad de consulta K-nearest: O(K log n) amortizado, O(n) peor caso
 * Memoria: O(n)
 */
class KdTreeKnn(
    points: List<Point>,
) {
    private val nodes: Array<Point?>
    private val size: Int

    init {
        size = points.size
        nodes = arrayOfNulls(size)
        buildTree(points.toMutableList(), 0, 0, size - 1)
    }

    /**
     * Construye el arbol KD recursivamente.
     * En cada nivel, particiona por la mediana de la coordenada correspondiente
     * (x en niveles pares, y en impares).
     */
    private fun buildTree(
        pts: MutableList<Point>,
        depth: Int,
        lo: Int,
        hi: Int,
    ) {
        if (lo > hi) return
        val mid = (lo + hi) / 2

        // Seleccionar mediana por la coordenada del nivel actual
        val useX = depth % 2 == 0
        nthElement(pts, lo, hi, mid, useX)
        nodes[mid] = pts[mid]

        buildTree(pts, depth + 1, lo, mid - 1)
        buildTree(pts, depth + 1, mid + 1, hi)
    }

    /**
     * Partial sort: coloca el elemento que estaria en posicion k si el array
     * estuviera ordenado, y particiona el array alrededor de el.
     * Complejidad: O(n) amortizado (quickselect).
     */
    private fun nthElement(
        pts: MutableList<Point>,
        lo: Int,
        hi: Int,
        k: Int,
        useX: Boolean,
    ) {
        if (lo >= hi) return
        var left = lo
        var right = hi
        val pivotVal = if (useX) pts[k].x else pts[k].y

        // Three-way partition around pivot
        var i = lo
        while (i <= right) {
            val v = if (useX) pts[i].x else pts[i].y
            if (v < pivotVal) {
                pts[left] = pts[i].also { pts[i] = pts[left] }
                left++
                i++
            } else if (v > pivotVal) {
                pts[right] = pts[i].also { pts[i] = pts[right] }
                right--
            } else {
                i++
            }
        }
        if (k < left) nthElement(pts, lo, left - 1, k, useX)
        if (k > right) nthElement(pts, right + 1, hi, k, useX)
    }

    /**
     * Encuentra los K vecinos mas cercanos a [query] (excluyendo query si esta en el arbol).
     *
     * @param query punto de consulta
     * @param k numero de vecinos a retornar
     * @return lista de hasta K puntos ordenados por distancia ascendente
     *
     * Complejidad: O(K log n) amortizado, O(n) peor caso
     */
    fun kNearest(
        query: Point,
        k: Int,
    ): List<Point> {
        // Max-heap de tamaño K: mantiene los K mas cercanos, con el mas lejano en la raiz.
        // Desempate determinista: distancia desc, luego x desc, luego y desc (para el max-heap).
        val heap =
            PriorityQueue<Pair<Point, Double>>(
                k + 1,
                compareByDescending<Pair<Point, Double>> { it.second }
                    .thenByDescending { it.first.x }
                    .thenByDescending { it.first.y },
            )

        searchKnn(query, k, 0, 0, size - 1, heap)

        return heap
            .sortedWith(compareBy<Pair<Point, Double>> { it.second }.thenBy { it.first.x }.thenBy { it.first.y })
            .map { it.first }
    }

    private fun searchKnn(
        query: Point,
        k: Int,
        depth: Int,
        lo: Int,
        hi: Int,
        heap: PriorityQueue<Pair<Point, Double>>,
    ) {
        if (lo > hi) return
        val mid = (lo + hi) / 2
        val node = nodes[mid] ?: return

        val dist = query.distance(node)
        if (node != query) { // Excluir el propio punto
            if (heap.size < k) {
                heap.add(Pair(node, dist))
            } else {
                val worst = heap.peek()
                // Reemplazar si es mas cercano, o en empate de distancia, si tiene coordenadas menores
                val isBetter =
                    dist < worst.second ||
                        (
                            dist == worst.second &&
                                (node.x < worst.first.x || (node.x == worst.first.x && node.y < worst.first.y))
                        )
                if (isBetter) {
                    heap.poll()
                    heap.add(Pair(node, dist))
                }
            }
        }

        val useX = depth % 2 == 0
        val diff = if (useX) query.x - node.x else query.y - node.y

        // Visitar el lado mas cercano primero
        val firstLo: Int
        val firstHi: Int
        val secondLo: Int
        val secondHi: Int
        if (diff <= 0) {
            firstLo = lo
            firstHi = mid - 1
            secondLo = mid + 1
            secondHi = hi
        } else {
            firstLo = mid + 1
            firstHi = hi
            secondLo = lo
            secondHi = mid - 1
        }

        searchKnn(query, k, depth + 1, firstLo, firstHi, heap)

        // Podar: solo visitar el otro lado si podria contener puntos mas cercanos o iguales
        // (<=) porque con desempate por coordenadas, puntos a igual distancia pueden ser mejores
        val worstDist = if (heap.size < k) Double.MAX_VALUE else heap.peek().second
        if (kotlin.math.abs(diff) <= worstDist) {
            searchKnn(query, k, depth + 1, secondLo, secondHi, heap)
        }
    }
}

/**
 * Construye listas de vecinos cercanos usando KD-tree.
 * Para cada punto, encuentra los K puntos mas cercanos.
 *
 * Misma semantica que [buildNeighborLists] pero con KD-tree:
 * O(n log n) para construir + O(n * K log n) para consultas = O(n * K * log n).
 * Con K constante: O(n log n) vs O(n^2 log n) de la version original.
 *
 * @param points conjunto de puntos
 * @param k numero de vecinos a mantener
 * @return mapa de punto -> lista de K vecinos mas cercanos (ordenados por distancia)
 *
 * Complejidad: O(n * K * log n) — con K constante: O(n log n)
 */
fun buildNeighborListsKdTree(
    points: Set<Point>,
    k: Int,
): Map<Point, List<Point>> {
    val pointList = points.toList()
    val kActual = minOf(k, pointList.size - 1)
    val tree = KdTreeKnn(pointList)

    return pointList.associateWith { point ->
        tree.kNearest(point, kActual)
    }
}
