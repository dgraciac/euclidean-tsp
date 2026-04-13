package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverM7 — M2 con las tres optimizaciones de LK-deep combinadas
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E059
 *
 * Combina los tres cambios de M4+M5+M6:
 * 1. LK-deep profundidad 3 (en vez de 5) — de E056/M4
 * 2. LK-deep con alphaNl K=7 (en vez de combinedNl K=14) — de E057/M5
 * 3. LK-deep solo pre-DB, LK(2) post-DB — de E058/M6
 *
 * Factor constante resultante: 7^3 = 343 (vs 14^5 = 537,824 de M2 — 1,568x menor).
 * Si la calidad se mantiene, M7 sera genuinamente mas rapido que Christofides en n>500.
 *
 * Complejidad e2e: O(n^2) con factor constante K^3=343
 */
class SolverM7 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val base = M2Base.buildBase(instance)

        // Rama A: LK-2 + DB-nl + LK-2 (identica a M2)
        val afterLk2 = linKernighan(base.afterTwoOpt, base.combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, base.combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, base.combinedNl))

        // Rama B: LK-deep(3, alphaNl) + DB-nl + LK(2) — las tres optimizaciones
        val afterLk5 = linKernighanDeep(base.afterTwoOpt, base.alphaNl, maxDepth = 3)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, base.combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighan(afterDb5, base.combinedNl))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
