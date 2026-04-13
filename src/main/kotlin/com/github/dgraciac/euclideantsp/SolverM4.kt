package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverM4 — M2 con LK-deep profundidad 3 (en vez de 5)
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E056
 *
 * Hipotesis: Reducir la profundidad de LK-deep de 5 a 3 reduce el factor constante
 * de K^5=537,824 a K^3=2,744 (196x menor) sin perder calidad significativa,
 * porque la poda por ganancia positiva corta la mayoria de ramas antes de prof. 5.
 *
 * Cambio unico respecto a M2: linKernighanDeep(maxDepth=3) en vez de maxDepth=5.
 *
 * Complejidad e2e: O(n^2) con factor constante K^3=2,744 (vs K^5=537,824 de M2)
 */
class SolverM4 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val base = M2Base.buildBase(instance)

        // Rama A: LK-2 + DB-nl + LK-2 (identica a M2)
        val afterLk2 = linKernighan(base.afterTwoOpt, base.combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, base.combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, base.combinedNl))

        // Rama B: LK-deep(3) + DB-nl + LK-deep(3)
        val afterLk5 = linKernighanDeep(base.afterTwoOpt, base.combinedNl, maxDepth = 3)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, base.combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, base.combinedNl, maxDepth = 3))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
