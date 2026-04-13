package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Point

/**
 * Parser de ficheros TSPLIB en formato EUC_2D.
 *
 * Lee un fichero .tsp desde los resources del classpath y construye una
 * [Euclidean2DTSPInstance] con los puntos y el optimo conocido.
 *
 * @param resourcePath ruta del fichero dentro de resources (ej: "tsplib/pcb3038.tsp")
 * @param optimalLength longitud del tour optimo conocido (de TSPLIB)
 * @return instancia TSP con los puntos parseados
 *
 * Complejidad: O(n) donde n es el numero de puntos.
 */
fun parseTspLibResource(
    resourcePath: String,
    optimalLength: Double,
): Euclidean2DTSPInstance {
    val stream =
        Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

    val lines = stream.bufferedReader().readLines()
    var name = ""
    var inCoords = false
    val points = mutableSetOf<Point>()

    for (line in lines) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("NAME") -> {
                name = trimmed.substringAfter(":").trim()
            }

            trimmed == "NODE_COORD_SECTION" -> {
                inCoords = true
            }

            trimmed == "EOF" -> {
                break
            }

            inCoords -> {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 3) {
                    points.add(Point(parts[1].toDouble(), parts[2].toDouble()))
                }
            }
        }
    }

    return Euclidean2DTSPInstance(
        name = name,
        optimalLength = optimalLength,
        points = points,
    )
}
