package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverM5 — M2 con LK-deep usando solo alpha-nearness K=7 (en vez de combined K=14)
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E057
 *
 * Hipotesis: En LK-deep, solo los candidatos alpha-nearness (K=7) son relevantes.
 * Los candidatos dist-only son redundantes en la fase deep. Esto reduce K^5 de
 * 537,824 (K=14) a 16,807 (K=7), un factor 32x. La fase shallow (LK-2, or-opt-nl,
 * 2-opt-nl, DB-nl) sigue usando K=14 combinados.
 *
 * Cambio unico respecto a M2: LK-deep usa alphaNl (K=7) en vez de combinedNl (K=14).
 *
 * Complejidad e2e: O(n^2) con factor constante K^5=16,807 (vs 537,824 de M2)
 */
class SolverM5 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val base = M2Base.buildBase(instance)

        // Rama A: LK-2 + DB-nl + LK-2 (identica a M2, usa combinedNl)
        val afterLk2 = linKernighan(base.afterTwoOpt, base.combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, base.combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, base.combinedNl))

        // Rama B: LK-deep(5, alphaNl) + DB-nl + LK-deep(5, alphaNl)
        val afterLk5 = linKernighanDeep(base.afterTwoOpt, base.alphaNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, base.combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighanDeep(afterDb5, base.alphaNl, maxDepth = 5))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
