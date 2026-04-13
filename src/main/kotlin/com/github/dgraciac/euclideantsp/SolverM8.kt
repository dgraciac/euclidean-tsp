package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverM8 — M2 con LK-deep(3, alphaNl K=7) manteniendo LK-deep post-DB
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E060
 *
 * Combina los dos cambios que funcionaron individualmente sin perder calidad excesiva:
 * - De M4: profundidad 3 en vez de 5 (speedup 2.5x, perdida max +0.008)
 * - De M5: alphaNl K=7 en vez de combinedNl K=14 (speedup 1.5x, sin perdida)
 * NO incluye M6 (sin post-DB) porque no aportaba velocidad y M7 que lo incluia
 * perdia calidad inaceptable en pcb442.
 *
 * Factor constante: 7^3 = 343 (vs 14^5 = 537,824 de M2 — 1,568x menor).
 * Mantiene LK-deep en ambas posiciones (pre-DB y post-DB) para preservar calidad.
 *
 * Complejidad e2e: O(n^2) con factor constante K^3=343
 */
class SolverM8 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val base = M2Base.buildBase(instance)

        // Rama A: LK-2 + DB-nl + LK-2 (identica a M2)
        val afterLk2 = linKernighan(base.afterTwoOpt, base.combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, base.combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, base.combinedNl))

        // Rama B: LK-deep(3, alphaNl) + DB-nl + LK-deep(3, alphaNl)
        val afterLk5 = linKernighanDeep(base.afterTwoOpt, base.alphaNl, maxDepth = 3)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, base.combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, base.alphaNl, maxDepth = 3))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
