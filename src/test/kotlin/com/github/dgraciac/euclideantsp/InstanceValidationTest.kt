package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Validacion de las instancias TSPLIB importadas.
 *
 * Verifica:
 * 1. Numero de puntos coincide con DIMENSION de TSPLIB
 * 2. Para instancias pequeñas: BruteForce encuentra un tour con longitud <= optimalLength
 *    (usando distancias euclideas reales, que son <= distancias TSPLIB redondeadas)
 * 3. Coherencia basica de los datos
 *
 * NOTA IMPORTANTE sobre distancias:
 * TSPLIB define EUC_2D como nint(sqrt(dx^2+dy^2)) — distancias redondeadas al entero.
 * Nuestro codigo usa distancias euclideas reales (sin redondeo).
 * Esto significa que nuestras longitudes de tour son ligeramente menores que las de TSPLIB.
 * Los ratios de aproximacion tienen un pequeño sesgo por esta discrepancia.
 */
internal class InstanceValidationTest {
    companion object {
        @JvmStatic
        fun allInstances(): Stream<Arguments> =
            Stream.of(
                // nombre, instancia, dimension esperada de TSPLIB
                Arguments.of("eil51", EIL_51, 51),
                Arguments.of("berlin52", BERLIN_52, 52),
                Arguments.of("st70", ST_70, 70),
                Arguments.of("eil76", EIL_76, 76),
                Arguments.of("rat99", RAT_99, 99),
                Arguments.of("kro200", KRO_200, 200),
                Arguments.of("a280", A_280, 279), // Original tiene 280 pero un punto duplicado fue eliminado
                Arguments.of("pcb442", PCB_442, 442),
            )

        @JvmStatic
        fun smallInstances(): Stream<Arguments> =
            Stream.of(
                Arguments.of(TRIVIAL),
                Arguments.of(instance4Square),
                Arguments.of(instance4A),
                Arguments.of(instance4B),
                Arguments.of(instance5A),
                Arguments.of(instance5B),
                Arguments.of(instance6A),
                Arguments.of(instance6B),
                Arguments.of(instance6C),
                Arguments.of(instance6D),
                Arguments.of(instance10A),
            )
    }

    @ParameterizedTest
    @MethodSource("allInstances")
    fun verify_point_count(
        name: String,
        instance: Euclidean2DTSPInstance,
        expectedDimension: Int,
    ) {
        println("$name: ${instance.points.size} puntos (esperado: $expectedDimension)")
        assertThat(instance.points.size)
            .describedAs("Numero de puntos de $name debe coincidir con DIMENSION de TSPLIB")
            .isEqualTo(expectedDimension)
    }

    @ParameterizedTest
    @MethodSource("allInstances")
    fun verify_no_duplicate_points(
        name: String,
        instance: Euclidean2DTSPInstance,
        expectedDimension: Int,
    ) {
        // Set ya elimina duplicados, asi que si el tamaño es correcto no hay duplicados
        assertThat(instance.points.size)
            .describedAs("$name no debe tener puntos duplicados")
            .isEqualTo(expectedDimension)
    }

    @ParameterizedTest
    @MethodSource("smallInstances")
    fun verify_brute_force_finds_optimal(instance: Euclidean2DTSPInstance) {
        val bruteForce = BruteForce()
        val optimalTour = bruteForce.compute(instance)

        // Nuestro tour con distancias reales debe ser <= optimalLength de TSPLIB
        // (porque distancias reales <= distancias redondeadas)
        println(
            "${instance.name}: BruteForce=${
                "%.6f".format(optimalTour.length)
            }, " +
                "declared optimal=${instance.optimalLength}, " +
                "ratio=${"%.6f".format(optimalTour.length / instance.optimalLength)}",
        )

        assertThat(optimalTour.length)
            .describedAs(
                "BruteForce tour de ${instance.name} debe ser <= optimalLength declarado " +
                    "(distancias reales <= redondeadas TSPLIB)",
            ).isLessThanOrEqualTo(instance.optimalLength + 0.001)
    }

    @ParameterizedTest
    @MethodSource("allInstances")
    fun report_distance_discrepancy(
        name: String,
        instance: Euclidean2DTSPInstance,
        expectedDimension: Int,
    ) {
        // Calcular la longitud del tour de Christofides con ambos metodos de distancia
        // para cuantificar la discrepancia
        val christofides = Christofides()
        val tour = christofides.compute(instance)

        val realLength = tour.length
        val tspLibLength = computeTspLibLength(tour.points)

        val discrepancy = (tspLibLength - realLength) / realLength * 100

        println(
            "$name: distancia real=${"%.2f".format(realLength)}, " +
                "distancia TSPLIB (nint)=${"%.0f".format(tspLibLength)}, " +
                "discrepancia=${"%.3f".format(discrepancy)}%",
        )
    }

    /**
     * Calcula la longitud de un tour usando la definicion TSPLIB de EUC_2D:
     * nint(sqrt(dx^2 + dy^2)) para cada arista.
     */
    private fun computeTspLibLength(points: List<com.github.dgraciac.euclideantsp.shared.Point>): Double {
        var total = 0.0
        for (i in 0 until points.size - 1) {
            val dx = points[i].x - points[i + 1].x
            val dy = points[i].y - points[i + 1].y
            total += sqrt(dx * dx + dy * dy).roundToInt()
        }
        return total
    }
}
