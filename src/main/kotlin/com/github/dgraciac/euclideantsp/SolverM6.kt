package com.github.dgraciac.euclideantsp

import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPInstance
import com.github.dgraciac.euclideantsp.shared.Euclidean2DTSPSolver
import com.github.dgraciac.euclideantsp.shared.Tour

/**
 * SolverM6 — M2 sin LK-deep post-DB (solo pre-DB)
 *
 * Linea de investigacion: M (escalabilidad sub-cubica)
 * Padre: SolverM2
 * Experimento: E058
 *
 * Hipotesis: Tras el double-bridge, el tour esta perturbado y las mejoras profundas
 * (que justifican LK-deep) son menos probables. Usar LK(2) post-DB en vez de LK-deep
 * elimina la mitad del coste de LK-deep. En J5, la rama A (LK-2 post-DB) gana en
 * ~50% de las instancias, lo que sugiere que LK-deep post-DB no es critico.
 *
 * Cambio respecto a M2: Rama B pasa de LK-deep+DB+LK-deep a LK-deep+DB+LK(2).
 *
 * Complejidad e2e: O(n^2) — una sola ejecucion de LK-deep en vez de dos.
 */
class SolverM6 : Euclidean2DTSPSolver {
    override fun compute(instance: Euclidean2DTSPInstance): Tour {
        val base = M2Base.buildBase(instance)

        // Rama A: LK-2 + DB-nl + LK-2 (identica a M2)
        val afterLk2 = linKernighan(base.afterTwoOpt, base.combinedNl)
        val afterDb2 = doubleBridgePerturbationNl(afterLk2, base.combinedNl, maxAttempts = 20)
        val tourA = Tour(points = linKernighan(afterDb2, base.combinedNl))

        // Rama B: LK-deep(5) + DB-nl + LK(2) (NO LK-deep post-DB)
        val afterLk5 = linKernighanDeep(base.afterTwoOpt, base.combinedNl, maxDepth = 5)
        val afterDb5 = doubleBridgePerturbationNl(afterLk5, base.combinedNl, maxAttempts = 20)
        val tourB = Tour(points = linKernighan(afterDb5, base.combinedNl))

        return if (tourA.length <= tourB.length) tourA else tourB
    }
}
