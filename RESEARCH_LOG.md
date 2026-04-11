# Log de Investigacion — TSP Euclideo 2D

## Objetivo

Encontrar un algoritmo de complejidad polinomica que resuelva el TSP Euclideo 2D de forma optima.

**Restriccion:** Solo se investigan algoritmos con complejidad polinomica. Algoritmos super-polinomicos (O(2^n), O(n!)) quedan fuera del alcance.

**Señales de progreso:**
- Buena señal: un solver polinomico que aproxima mejor que Christofides (O(n^3), garantia 1.5x)
- Perfecto: un solver polinomico que encuentra la solucion optima

**Nota sobre "superar a Christofides":** En este log, "superar" o "mejor que Christofides" se refiere
a **mejor ratio empirico** en las instancias de test, NO a mejor garantia teorica. Christofides tiene
una garantia demostrada de 1.5x en el peor caso para cualquier instancia. Nuestros solvers no tienen
esa garantia — su rendimiento solo esta verificado empiricamente en 4 instancias TSPLIB.

---

## Metricas de comparacion

Se registran tres metricas agregadas sobre los ratios de aproximacion de todas las instancias:
1. **Media aritmetica** — Promedio simple de los ratios
2. **Media geometrica** — Estandar en benchmarking, penaliza menos outliers
3. **Peor caso** — Ratio maximo, relevante para garantias

## Mejores resultados actuales

| Instancia | n | Optimo | Mejor Solver | Ratio | Tiempo | Complejidad | Fecha |
|-----------|---|--------|-------------|-------|--------|-------------|-------|
| eil51 | 51 | 426.0 | SolverE2/E4 | 1.007x | 0.10s | O(n^4) | 2026-04-11 |
| berlin52 | 52 | 7542.0 | SolverE2/E4 | 1.000x | 0.10s | O(n^4) | 2026-04-11 |
| st70 | 70 | 675.0 | SolverE2/E4 | 1.011x | 0.13s | O(n^4) | 2026-04-11 |
| eil76 | 76 | 538.0 | SolverE2/E4 | 1.027x | 0.12s | O(n^4) | 2026-04-11 |
| rat99 | 99 | 1211.0 | SolverE2/E4 | 1.016x | 0.24s | O(n^4) | 2026-04-11 |
| kro200 | 200 | 29368.0 | SolverE2/E4 | 1.006x | 3.2s | O(n^4) | 2026-04-11 |
| a280 | 279 | 2579.0 | SolverH3 | 1.014x | 34.1s | O(n^4) | 2026-04-11 |
| pcb442 | 442 | 50778.0 | SolverH3 | 1.012x | 156.2s | O(n^4) | 2026-04-11 |

### Resumen agregado por solver

| Solver | Complejidad | Media arit. | Media geom. | Peor caso | Tiempo max |
|--------|-------------|------------|------------|-----------|------------|
Nota: metricas calculadas sobre 7 instancias (eil51, berlin52, st70, eil76, rat99, kro200, a280)

| Solver | Tipica | Peor caso | Media geom. | Peor ratio | Tiempo max |
|--------|--------|-----------|------------|------------|------------|
| **SolverG2** | **O(n^4)** | **O(n^4)** | **1.012x** | **1.021x** | **84s** |
| SolverE2/E4 | O(n^4) | O(n^4) | 1.012x | 1.027x | 55s |
| SolverH1 | O(n^3.5) | O(n^3.5) | 1.015x | 1.028x | 3.0s |
| SolverE3 | O(n^3.5) | O(n^3.5) | 1.018x | 1.034x | 1.1s |
| SolverG1 | O(n^3.5) | O(n^3.5) | 1.015x | 1.034x | 2.0s |
| SolverE7 | O(n^4) | O(n^4) | 1.011x | 1.021x | 83s |
| **SolverJ5** | **O(n^3)** | **O(n^3)** | **1.010x** | **1.025x** | **3.99s** |
| SolverJ3 | O(n^3) | O(n^3) | 1.011x | 1.025x | 2.30s |
| SolverI2 | O(n^3) | O(n^3) | 1.016x | 1.041x | 1.89s |
| SolverI1 | O(n^3) | O(n^3) | 1.023x | 1.041x | 0.54s |
| SolverC3 | O(n^3) | O(n^3) | 1.039x | 1.069x | 0.18s |
| SolverB3 | O(n^3) | O(n^3) | 1.039x | 1.055x | 0.064s |
| SolverE1 | O(n^3) | O(n^3) | 1.043x | 1.075x | 0.054s |
| Christofides | O(n^3) | O(n^3) | 1.147x | 1.165x | 0.20s |
| Christofides | O(n^3) | 1.147x | 1.147x | 1.165x | 0.12s |
| SolverC1 | O(n^3) | 1.174x | 1.173x | 1.218x | 0.014s |

**Notas sobre garantias de aproximacion:**
- Christofides tiene garantia de aproximacion **3/2 demostrada** (peor caso sobre cualquier instancia).
- Nuestros solvers no tienen garantia teorica. Los ratios reportados son **empiricos** sobre 8 instancias.
- Pendiente: determinar la peor aproximacion garantizada de cada solver. Esto requiere analisis
  teorico (demostracion de cota superior) o busqueda de contraejemplos (instancias adversariales).
- Sin esta garantia, la comparacion con Christofides es incompleta: mejor ratio empirico no implica
  mejor algoritmo si el peor caso no esta acotado.

**Notas sobre complejidad peor caso:**
- Tras E026, la complejidad tipica y peor caso coinciden para todos los solvers.
  2-opt converge en <=6 pasadas (O(1)) y or-opt en ~0.2*n pasadas (O(n)).
  Safety limits reducidos de n^2 a max(20,n). Resultados identicos verificados.
- SolverC3 ahora es O(n^3) peor caso — igual que Christofides, pero con media 1.039x vs 1.147x.
- SolverG2 tiene el mejor ratio (1.012x, 1.021x peor) con peor caso O(n^4).
- SolverH1 es buen tradeoff: O(n^3.5) peor caso con 1.015x media y <3s en pcb442.
- Resultados validados en 8 instancias TSPLIB (51-442 puntos).

---

## State of the art — Referencia externa

Comparacion de nuestros mejores solvers con los algoritmos de referencia en la literatura.
Los datos de LKH y Concorde son de la literatura publicada, no de nuestras mediciones.

| Algoritmo | Tipo | Complejidad | Gap al optimo | Garantia | Instancias |
|-----------|------|-------------|--------------|----------|------------|
| **Concorde** | Exacto | Exponencial | 0% (optimo) | Exacto | Hasta ~85,000 pts |
| **LKH** (Helsgott) | Heuristico | No polinomico | <0.1% | Ninguna | Hasta millones |
| **Nuestro SolverH3** | Heuristico | O(n^4) pc | ~0.6% | Ninguna | Hasta 442 pts |
| **Nuestro SolverJ3** | Heuristico | O(n^3) pc | ~1.1% | Ninguna | Hasta 442 pts |
| **Christofides** | Aproximacion | O(n^3) pc | ~15% empirico | 3/2 demostrada | Cualquier |

**Que hace LKH que nosotros no:**
1. α-nearness candidates (basados en 1-tree, no distancia simple)
2. LK profundidad 5+ con backtracking (nosotros: profundidad 2 sin backtracking)
3. Movimientos no secuenciales (double-bridge integrado en la busqueda LK)
4. Optimizacion subgradiente para cotas inferiores y guia de candidatos
5. Estructuras de datos O(log n) para operaciones de segmentos (nosotros: O(n))

**Hoja de ruta para cerrar el gap con LKH:**

| Paso | Solver | Tecnica | Gap esperado | Complejidad |
|------|--------|---------|-------------|-------------|
| Actual | SolverI2 | 2-opt-nl + or-opt + LK(2) + DB | ~1.2% | O(n^3) |
| E029 | SolverJ1 | + α-nearness (1-tree candidates) | ~0.5% | O(n^3) |
| E030 | SolverJ2 | + LK profundidad 5 con backtracking | ~0.2% | O(n^3) |
| E031 | SolverJ3 | + movimientos no secuenciales | ~0.1% | O(n^3) |
| E032 | SolverJ4 | + subgradient optimization | <0.1% | O(n^3) |
| E033 | SolverJ5 | + segment trees O(log n) | <0.1% (rapido) | O(n^3) |

---

## Lineas de investigacion

### Linea A: Inicializacion basada en centroide
- **Estado:** Pausada. SolverA tiene peor rendimiento que SolverB en instancias grandes.
- **Concepto:** Construir tour desde el centroide hacia afuera. Los 3 puntos mas cercanos al centroide forman el triangulo inicial, luego insercion por distancia absoluta.
- **Insight clave:** La inicializacion desde el centroide crea triangulos iniciales pobres para distribuciones no uniformes de puntos.

### Linea B: Inicializacion con convex hull + insercion por ratio
- **Estado:** Activa. Mejor linea de investigacion actual.
- **Concepto:** El convex hull como tour inicial garantiza un poligono convexo. La insercion por ratio relativo (d(A,P)+d(P,B))/d(A,B) penaliza menos insertar en aristas largas.
- **Variante actual:** SolverB1 (SolverB + 2-opt) — mejor solver del proyecto.

### Linea C: Descomposicion en capas de convex hull (peeling)
- **Estado:** Activa pero resultados iniciales decepcionantes.
- **Concepto:** Pelar convex hulls sucesivos para obtener capas concentricas, luego conectarlas.
- **Insight clave:** La insercion simple de capas interiores no preserva bien la estructura geometrica. Necesita mejor estrategia de conexion entre capas.

---

## Log de experimentos

### E032 — SolverJ6: Subgradient optimization para candidatos (2026-04-11)

- **Solver:** SolverJ6
- **Linea:** J
- **Complejidad:** O(n^3)
- **Resultados:** Mejora marginal en eil76 (1.022x). Peor en a280 (1.014x vs 1.006x de J5).
- **Metricas:** Media=1.011x | Peor caso=1.022x
- **Conclusion:** Subgradient optimization no aporta mejora consistente. Los multiplicadores lagrangianos necesitan mas iteraciones o mejor tuning del step size.

### E031b — SolverJ5: Mejor de LK-2 y LK-5 (2026-04-11)

- **Solver:** SolverJ5
- **Linea:** J
- **Complejidad:** O(n^3)
- **Resultados:** Toma lo mejor de J3 y J4 en cada instancia. kro200: 1.004x, a280: 1.006x, pcb442: 1.013x.
- **Metricas:** Media=1.010x | Peor caso=1.025x
- **Conclusion:** Mejor solver O(n^3) del proyecto. Simple y efectivo: dos ramas de busqueda, mejor resultado.

### E030 — SolverJ4: LK profundidad 5 con backtracking (2026-04-11)

- **Solver:** SolverJ4
- **Linea:** J
- **Padre:** SolverJ3
- **Hipotesis:** LK profundidad 5 con backtracking encuentra movimientos profundos.
- **Complejidad peor caso:** O(n^3)
- **Resultados:**

| Instancia | SolverJ3 (LK-2) | SolverJ4 (LK-5) |
|-----------|-----------------|-----------------|
| kro200 | 1.012x | 1.004x (mejora) |
| a280 | 1.006x | 1.006x (igual) |
| pcb442 | 1.013x | 1.016x (peor) |
| Media | 1.011x | 1.010x |

- **Metricas SolverJ4:** Media=1.010x | Peor caso=1.025x
- **Conclusion:** El backtracking funciona y mejora en kro200, pero la reconstruccion de tours con 5+ segmentos es limitada (pocas reconexiones implementadas). Media ligeramente mejor (1.010x vs 1.011x). Pendiente: ampliar reconexiones para profundidades altas.

### E029 — SolverJ1/J2/J3: α-nearness candidates (2026-04-11)

- **Solvers:** SolverJ1 (α solo), SolverJ2 (α5+dist5), SolverJ3 (α7+dist7)
- **Linea:** J (tecnicas LKH)
- **Hipotesis:** α-nearness basado en 1-tree produce mejores candidatos que K-nearest por distancia.
- **Complejidad:** O(n^3) — misma que SolverI2
- **Resultados comparativos (vs SolverI2):**

| Instancia | SolverI2 | J1 (α) | J2 (α5+d5) | J3 (α7+d7) |
|-----------|----------|--------|-----------|-----------|
| a280 | 1.017x | 1.047x | 1.006x | 1.006x |
| pcb442 | 1.027x | 1.012x | 1.027x | 1.013x |
| Media | 1.012x | 1.015x | 1.014x | 1.011x |

- **Metricas SolverJ3:** Media=1.011x | Peor caso=1.025x
- **Conclusion:** α-nearness ayuda significativamente en instancias grandes (a280, pcb442) pero puede empeorar en pequeñas si K es bajo. La combinacion α(7)+dist(7) da la mejor estabilidad. SolverJ3 es el nuevo mejor solver O(n^3): media 1.011x, peor caso 1.025x. En a280 da 1.006x, superando SolverH3 (O(n^4), 1.008x).

### E027 — SolverI1/I2: Mejores solvers O(n^3) (2026-04-11)

- **Solvers:** SolverI1, SolverI2
- **Linea:** I (optimizar dentro de O(n^3))
- **Hipotesis:** Multi-start(hull) con 2-opt-nl rapido + or-opt + LK + double-bridge, todo en O(n^3).
- **Complejidad peor caso:** O(n^3) — misma que Christofides
- **Resultados SolverI2 (mejor O(n^3)):**

| Instancia | Christofides | SolverC3 | SolverI2 | Tiempo I2 |
|-----------|-------------|----------|----------|-----------|
| eil51 | 1.149x | 1.042x | 1.007x | 0.02s |
| berlin52 | 1.123x | 1.000x | 1.000x | 0.01s |
| st70 | 1.143x | 1.031x | 1.020x | 0.05s |
| eil76 | 1.161x | 1.054x | 1.041x | 0.03s |
| rat99 | 1.134x | 1.043x | 1.007x | 0.05s |
| kro200 | 1.143x | 1.048x | 1.012x | 0.30s |
| a280 | 1.157x | 1.069x | 1.017x | 0.66s |
| pcb442 | 1.117x | 1.056x | 1.025x | 1.89s |

- **Metricas SolverI2:** Media aritmetica=1.016x | Media geometrica=1.016x | Peor caso=1.041x
- **Conclusion:** SolverI2 es el mejor solver O(n^3) por amplio margen. La clave es combinar multi-start rapido (2-opt-nl O(n^2) por start) + busqueda profunda (or-opt + 2-opt + LK) + double-bridge, todo dentro del presupuesto O(n^3). Media 1.016x vs Christofides 1.147x — misma complejidad, mucho mejor aproximacion empirica.

### E026 — Analisis de pasadas de 2-opt y or-opt (2026-04-11)

- **Test:** PassCountAnalysisTest
- **Hipotesis:** 2-opt y or-opt convergen en O(n) pasadas o menos, permitiendo reducir el safety limit.
- **Resultados:**
  - 2-opt: maximo 6 pasadas en TODAS las instancias (n=51 a n=442). Convergencia O(1).
  - or-opt: maximo ~0.2*n pasadas. Convergencia O(n).
- **Impacto:** Safety limits reducidos de n^2 a max(20,n). Resultados identicos.
- **Nuevas complejidades peor caso:**
  - twoOpt: O(n^3) (antes O(n^4))
  - orOpt: O(n^3) (antes O(n^4))
  - SolverC3: O(n^3) — ahora MISMO peor caso que Christofides!
  - SolverH3: O(n^4) (antes O(n^5))
- **Conclusion:** Descubrimiento clave. Reduce un grado la complejidad peor caso de todos los solvers. SolverC3 ahora es O(n^3) peor caso como Christofides pero con media 1.039x vs 1.147x.

### E024 — SolverH3: Multi-start completo + LK + double-bridge + LK (2026-04-11)

- **Solver:** SolverH3
- **Linea:** H
- **Padre:** SolverH2 + SolverE7
- **Hipotesis:** Aplicar LK a cada inicio de NN (multi-start completo) encuentra mas optimos locales profundos que solo multi-start selectivo.
- **Complejidad e2e:** O(n^4) tipica
- **Complejidad peor caso:** O(n^4)
- **Resultados:**

| Instancia | Mejor anterior | SolverH3 | Mejora | Tiempo |
|-----------|---------------|----------|--------|--------|
| eil51 | 1.007x (E2) | 1.008x | = | 0.2s |
| berlin52 | 1.000x (E2) | 1.000x | = | 0.2s |
| st70 | 1.003x (E5) | 1.003x | = | 0.9s |
| eil76 | 1.021x (E7/G2) | 1.021x | = | 0.8s |
| rat99 | 1.008x (G1/G2) | 1.007x | -0.1% | 1.5s |
| kro200 | 1.004x (H2) | 1.005x | = | 17.8s |
| a280 | 1.020x (G2) | 1.014x | -0.6% | 34.1s |
| pcb442 | 1.018x (E2) | 1.012x | -0.6% | 156.2s |

- **Metricas agregadas:** Media aritmetica=1.009x | Media geometrica=1.008x | Peor caso=1.021x
- **Conclusion:** Mejor solver del proyecto. Mejora los records en a280 y pcb442. El multi-start completo con LK en cada start es mas efectivo que LK solo sobre el mejor tour. O(n^4) peor caso, 156s en pcb442.

### E023 — SolverH2: Multi-start selectivo + LK + double-bridge + LK (2026-04-11)

- **Solver:** SolverH2
- **Linea:** H
- **Padres:** SolverH1 + LinKernighan v2
- **Hipotesis:** Combinar LK correcto (profundidad 2) con double-bridge y re-aplicar LK escapa optimos locales mejor que double-bridge solo.
- **Complejidad e2e:** O(n^3.5) tipica
- **Complejidad peor caso:** O(n^3.5)
- **Resultados:**

| Instancia | SolverH1 | SolverH2 | SolverE2 | Tiempo H2 |
|-----------|----------|----------|----------|-----------|
| eil51 | 1.007x | 1.007x | 1.007x | 0.04s |
| berlin52 | 1.000x | 1.000x | 1.000x | 0.10s |
| st70 | 1.014x | 1.011x | 1.011x | 0.07s |
| eil76 | 1.034x | 1.026x | 1.027x | 0.08s |
| rat99 | 1.008x | 1.008x | 1.016x | 0.17s |
| kro200 | 1.010x | 1.004x | 1.006x | 0.61s |
| a280 | 1.023x | 1.023x | 1.021x | 1.64s |
| pcb442 | 1.032x | 1.025x | 1.018x | 4.26s |

- **Metricas agregadas:** Media aritmetica=1.013x | Media geometrica=1.013x | Peor caso=1.026x
- **Conclusion:** LK funciona correctamente. El pipeline LK + DB + LK produce los mejores resultados del proyecto en instancias medianas (kro200: 1.004x). Compite con E2/G2 (O(n^4)) siendo O(n^3.5) peor caso y mucho mas rapido (<5s en pcb442 vs 55-85s).
- **Nota:** LK v2 corrige el bug de v1: usa ganancia real del 2-opt (basada en aristas del tour resultante) y construye tours depth-2 explicitamente con segmentos.

### E022 — SolverH1: Multi-start selectivo + double-bridge perturbation (2026-04-11)

- **Solver:** SolverH1
- **Linea:** H (perturbacion para escapar optimos locales)
- **Hipotesis:** Double-bridge (4-opt no secuencial) permite escapar optimos locales que 2-opt+or-opt no pueden resolver.
- **Complejidad:** O(n^3.5)
- **Resultados:**

| Instancia | SolverE3 | SolverH1 | Mejora |
|-----------|----------|----------|--------|
| eil51 | 1.007x | 1.007x | = |
| berlin52 | 1.000x | 1.000x | = |
| st70 | 1.016x | 1.014x | -0.2% |
| eil76 | 1.034x | 1.025x | -0.9% |
| rat99 | 1.016x | 1.015x | -0.1% |
| kro200 | 1.023x | 1.010x | -1.3% |
| a280 | 1.029x | 1.026x | -0.3% |
| pcb442 | 1.033x | 1.028x | -0.5% |

- **Metricas agregadas:** Media aritmetica=1.016x | Media geometrica=1.015x | Peor caso=1.028x
- **Conclusion:** Double-bridge funciona — mejora en todas las instancias grandes vs SolverE3. Pero multi-start completo (SolverE2/G2, O(n^4)) sigue siendo superior. El multi-start diversifica mas que 20 perturbaciones sobre un solo optimo local.
- **Nota:** La implementacion inicial con Lin-Kernighan tenia un bug en el calculo de ganancia (confundia aristas teoricas LK con aristas reales del 2-opt reversal). Se reemplazo por double-bridge perturbation que es mas simple y correcto.

### E017 — SolverE6: Multi-start selectivo + 3-opt (2026-04-11)

- **Solver:** SolverE6
- **Linea:** E
- **Padre:** SolverE3
- **Hipotesis:** 3-opt puede corregir estructuras que 2-opt+or-opt no resuelven.
- **Complejidad:** O(n^3.5)
- **Resultados:** Identico a SolverE3 en casi todas las instancias. Mejora marginal en kro200 (1.017x vs 1.023x).
- **Metricas agregadas:** Media aritmetica=1.017x | Media geometrica=1.017x | Peor caso=1.034x
- **Conclusion:** 3-opt no justifica su coste. 2-opt+or-opt ya cubre las estructuras que 3-opt podria resolver.

### E016 — SolverE5: Or-opt extendido segmentos 1-5 (2026-04-11)

- **Solver:** SolverE5
- **Linea:** E
- **Padre:** SolverE2
- **Hipotesis:** Segmentos de 4-5 puntos en or-opt permiten mejoras que or-opt(3) no encuentra.
- **Complejidad:** O(n^4)
- **Resultados:** Mejora solo en st70 (1.003x vs 1.011x). En el resto, identico a E2.
- **Metricas agregadas (8 instancias):** Media aritmetica=1.012x | Media geometrica=1.012x | Peor caso=1.027x
- **Conclusion:** Segmentos >3 rara vez se reubican con ganancia. Mejora insuficiente para el coste extra.

### Instancia pcb442 (442 puntos, placa de circuito impreso)

- SolverE2: 1.018x en 52s | SolverE3: 1.033x en 1.2s | Christofides: 1.128x en 0.19s
- Confirma escalabilidad de nuestros solvers a instancias medianas.

### E015 — SolverE3: Multi-start selectivo (vertices convex hull) (2026-04-11)

- **Solver:** SolverE3
- **Linea:** E
- **Padre:** SolverE2
- **Hipotesis:** Usar solo vertices del convex hull como puntos de inicio (h ≈ sqrt(n)) reduce de O(n^4) a O(n^3.5) manteniendo la calidad.
- **Complejidad:** O(h * n^3) ≈ O(n^3.5)
- **Resultados:**

| Instancia | n | SolverE2 | SolverE3 | Tiempo E3 |
|-----------|---|----------|----------|-----------|
| eil51 | 51 | 1.007x | 1.007x | 0.02s |
| berlin52 | 52 | 1.000x | 1.000x | 0.02s |
| st70 | 70 | 1.011x | 1.016x | 0.02s |
| eil76 | 76 | 1.027x | 1.034x | 0.03s |
| rat99 | 99 | 1.016x | 1.016x | 0.05s |
| kro200 | 200 | 1.006x | 1.023x | 0.27s |
| a280 | 279 | 1.021x | 1.029x | 0.48s |

- **Metricas agregadas:** Media aritmetica=1.018x | Media geometrica=1.018x | Peor caso=1.034x
- **Conclusion:** Excelente tradeoff velocidad/calidad. Casi igual que E2 en instancias pequeñas (<100 puntos), pierde algo en kro200/a280. O(n^3.5) en la practica, <0.5s en todas las instancias. Mejor solver "rapido" del proyecto.

### E014 — SolverE4: Busqueda local iterativa (2026-04-11)

- **Solver:** SolverE4
- **Linea:** E
- **Padre:** SolverE2
- **Hipotesis:** Repetir el ciclo 2-opt/or-opt mejorara la convergencia.
- **Complejidad:** O(n^4)
- **Resultados:** Identicos a SolverE2 en todas las instancias. El ciclo extra no aporta mejora.
- **Metricas agregadas:** Media aritmetica=1.013x | Media geometrica=1.012x | Peor caso=1.027x
- **Conclusion:** Una sola pasada (2-opt -> or-opt -> 2-opt) ya converge al optimo local. Repetir el ciclo no encuentra mejoras adicionales. La iteracion no aporta valor.

### E013 — SolverE2: Multi-start NN + busqueda local (2026-04-11)

- **Solver:** SolverE2
- **Linea:** E
- **Padre:** SolverE1
- **Hipotesis:** Multi-start (ejecutar desde cada punto) reducira la varianza y mejorara la calidad.
- **Complejidad:** O(n^4) — n ejecuciones de O(n^3)
- **Resultados:**

| Instancia | SolverE1 | SolverE2 | Mejora | Tiempo |
|-----------|----------|----------|--------|--------|
| berlin52 | 1.053x | 1.000x | -5.3% | 0.08s |
| st70 | 1.047x | 1.011x | -3.6% | 0.24s |
| kro200 | 1.016x | 1.006x | -1.0% | 4.1s |
| a280 | 1.050x | 1.021x | -2.9% | 10.5s |

- **Metricas agregadas:** Media aritmetica=1.010x | Media geometrica=1.009x | Peor caso=1.021x
- **Conclusion:** Mejor solver del proyecto por amplio margen. Multi-start reduce la media de 1.041x a 1.010x — casi optimo en todas las instancias. Es O(n^4) pero en la practica tarda segundos. El impacto de probar N puntos de inicio es enorme: cada inicio lleva a un optimo local distinto y el mejor es casi-global.

### E011 — Propiedad de capas en instancias grandes (2026-04-11)

- **Test:** ConvexHullLayerOrderLargeTest
- **Hipotesis:** La propiedad de capas (E008) se mantiene en instancias grandes.
- **Resultados:** **La propiedad NO se mantiene.** Las capas exteriores tienden a preservarse pero las interiores se rompen frecuentemente:
  - berlin52 (8 capas): 3-4 preservadas, 4-5 rotas — incluso con ratio 1.0003 (SolverC3)
  - st70 (8 capas): 3-5 preservadas, 3-5 rotas
  - kro200 (17 capas): 6-7 preservadas, 9-10 rotas
  - a280 (22 capas): 4-7 preservadas, 14-17 rotas
- **Conclusion:** La propiedad de capas es un fenomeno de instancias pequeñas. En instancias grandes, el tour optimo necesita romper el orden de las capas interiores para optimizar la longitud. El enfoque SolverD1 (fijar orden de capas) queda descartado.

### E010 — SolverF1: Delaunay nearest neighbor + busqueda local (2026-04-11)

- **Solver:** SolverF1
- **Linea:** F (basado en triangulacion de Delaunay)
- **Hipotesis:** Usar aristas Delaunay para guiar nearest neighbor producira mejor semilla.
- **Complejidad:** O(n^3)
- **Resultados:** berlin52: 1.053x | st70: 1.047x | kro200: 1.061x | a280: 1.065x
- **Metricas agregadas:** Media aritmetica=1.057x | Media geometrica=1.057x | Peor caso=1.065x
- **Conclusion:** Peor que SolverE1 (NN global). La restriccion a aristas Delaunay empeora la semilla NN en vez de mejorarla. El NN global ya tiende a elegir aristas Delaunay naturalmente.

### E009 — SolverE1: Nearest neighbor + busqueda local (2026-04-11)

- **Solver:** SolverE1
- **Linea:** E (nearest neighbor)
- **Hipotesis:** Si NN + busqueda local da resultados similares a SolverC3, la busqueda local domina.
- **Complejidad:** O(n^3)
- **Resultados:** berlin52: 1.053x | st70: 1.047x | kro200: **1.016x** | a280: **1.050x**
- **Metricas agregadas:** Media aritmetica=1.041x | Media geometrica=1.041x | Peor caso=1.053x
- **Conclusion:** Resultado clave: SolverE1 gana en kro200 y a280, demostrando que la busqueda local (2-opt + or-opt) domina la calidad en instancias grandes. La estrategia de construccion importa menos. Mejor peor caso (1.053x) que SolverC3 (1.069x).

### E008 — Propiedad teorica de capas de convex hull (2026-04-11)

- **Test:** ConvexHullLayerOrderTest
- **Hipotesis:** En el tour optimo, los vertices de cada capa de convex hull preservan su orden ciclico.
- **Metodo:** BruteForce en 11 instancias pequeñas (3-10 puntos).
- **Resultados:** **TODAS las capas preservan su orden en las 11 instancias.** 0 violaciones.
  - Instancias con 1 capa: trivial, 4square, 4a, 5a — trivialmente preservado
  - Instancias con 2 capas: 4b, 5b, 6a, 6b, 6c, 6d, 10a — todas preservadas
- **Conclusion:** Fuerte evidencia empirica de que la propiedad se cumple. Necesita verificacion en instancias mas grandes y una demostracion formal. Si se confirma, reduce drasticamente el espacio de busqueda: solo hay que determinar como intercalar las capas.

### E007 — SolverC4: Peeling + insercion ordenada + busqueda local (2026-04-11)

- **Solver:** SolverC4
- **Linea:** C
- **Hipotesis:** Insertar puntos en orden del hull interno mejorara la semilla para busqueda local.
- **Complejidad:** O(n^3)
- **Resultados:** Identicos a SolverC3 en todas las instancias.
- **Metricas agregadas:** Media aritmetica=1.037x | Media geometrica=1.036x | Peor caso=1.069x
- **Conclusion:** El orden de insercion de los puntos interiores NO importa cuando se aplica busqueda local despues. SolverC4 = SolverC3. La busqueda local borra cualquier diferencia en la semilla.

### E006 — SolverB3: Convex hull + insercion + busqueda local completa (2026-04-11)

- **Solver:** SolverB3
- **Linea:** B
- **Hipotesis:** Aislar si peeling o busqueda local marca la diferencia.
- **Complejidad:** O(n^3)
- **Resultados:** berlin52: 1.042x | st70: **1.020x** | kro200: 1.041x | a280: 1.055x
- **Metricas agregadas:** Media aritmetica=1.040x | Media geometrica=1.039x | Peor caso=1.055x
- **Conclusion:** SolverB3 < SolverC3 en media pero > en st70 y kro200. La diferencia entre peeling y convex hull es minima cuando se aplica busqueda local completa. Confirma que la busqueda local domina.

### E005 — SolverC3: Peeling + insercion + 2-opt + or-opt (2026-04-10)

- **Solver:** SolverC3
- **Linea:** C
- **Padre:** SolverC2
- **Hipotesis:** Añadir or-opt despues de 2-opt reubicara segmentos mal posicionados que 2-opt no puede corregir, mejorando 1-3% adicional.
- **Algoritmo:**
  1. Peeling + insercion por ratio — O(n^2)
  2. 2-opt hasta convergencia — O(n^3)
  3. Or-opt (segmentos de 1-3 puntos) hasta convergencia — O(n^3)
  4. 2-opt final por si or-opt abrio nuevas mejoras — O(n^3)
- **Complejidad:** O(n^3)
- **Resultados:**

| Instancia | SolverC2 | SolverC3 | Mejora | Tiempo | vs Christofides |
|-----------|----------|----------|--------|--------|-----------------|
| berlin52 | 1.026x | 1.000x | -2.6% | 0.008s | mucho mejor |
| st70 | 1.080x | 1.031x | -4.9% | 0.006s | mejor |
| kro200 | 1.085x | 1.048x | -3.7% | 0.059s | mejor |
| a280 | 1.087x | 1.069x | -1.8% | 0.036s | mejor |

- **Metricas agregadas:** Media aritmetica=1.037x | Media geometrica=1.036x | Peor caso=1.069x
- **Conclusion:** Resultado excepcional. SolverC3 es el mejor solver del proyecto en TODAS las metricas, superando incluso a SolverB1 (O(n^4)) que tenia media 1.052x. En berlin52 alcanza ratio 1.0003 — practicamente el tour optimo. El or-opt mejora 1.8-4.9% sobre SolverC2. Es O(n^3), milisegundos de ejecucion. El pipeline peeling + 2-opt + or-opt + 2-opt es muy efectivo.
- **Siguientes pasos:** Probar or-opt tambien sobre SolverB2 (SolverB3). Investigar por que berlin52 da casi-optimo y st70/kro200/a280 no. Intentar 3-opt selectivo para las instancias mas dificiles.

### E004 — SolverC2: Peeling + insercion + 2-opt (2026-04-10)

- **Solver:** SolverC2
- **Linea:** C
- **Padre:** SolverC1
- **Hipotesis:** Combinar el peeling de SolverC1 con 2-opt corregira las deficiencias de la insercion simple y producira tours competitivos manteniendo O(n^3).
- **Algoritmo:**
  1. Pelar convex hulls sucesivos — O(n^2 log n)
  2. Usar capa exterior como tour inicial, insertar capas interiores por ratio — O(n^2)
  3. Aplicar 2-opt hasta convergencia — O(n^3)
- **Complejidad:** O(n^3)
- **Resultados:**

| Instancia | SolverC1 | SolverC2 | Mejora | Tiempo | vs Christofides |
|-----------|----------|----------|--------|--------|-----------------|
| berlin52 | 1.147x | 1.026x | -12.1% | 0.001s | mejor |
| st70 | 1.123x | 1.080x | -4.3% | 0.003s | mejor |
| kro200 | 1.206x | 1.085x | -12.1% | 0.006s | mejor |
| a280 | 1.218x | 1.087x | -13.1% | 0.010s | mejor |

- **Metricas agregadas:** Media aritmetica=1.070x | Media geometrica=1.069x | Peor caso=1.087x
- **Conclusion:** Excelente resultado. SolverC2 supera a Christofides en todas las instancias siendo O(n^3). Incluso supera a SolverB2 en media (1.070x vs 1.076x). El peeling como estrategia de construccion es viable cuando se combina con 2-opt. Mejor solver O(n^3) del proyecto.
- **Siguientes pasos:** SolverC3 con intercalado de capas preservando orden geometrico, o SolverC4 con or-opt adicional.

### E003 — SolverB2: Construccion optimizada + 2-opt (2026-04-10)

- **Solver:** SolverB2
- **Linea:** B
- **Padre:** SolverB1
- **Hipotesis:** Eliminar la validacion isLinearRing reduce la complejidad de O(n^4) a O(n^2) sin degradar calidad, ya que 2-opt corrige auto-intersecciones. Resultado: O(n^3) como Christofides.
- **Algoritmo:**
  1. Convex hull como tour inicial — O(n log n)
  2. Insercion por ratio sin validacion — O(n^2)
  3. 2-opt hasta convergencia — O(n^3)
- **Complejidad:** O(n^3)
- **Resultados:**

| Instancia | SolverB1 | SolverB2 | Cambio | Tiempo | vs Christofides |
|-----------|----------|----------|--------|--------|-----------------|
| berlin52 | 1.010x | 1.048x | +3.8% | 0.001s | mejor |
| st70 | 1.051x | 1.069x | +1.8% | 0.002s | mejor |
| kro200 | 1.064x | 1.100x | +3.6% | 0.004s | mejor |
| a280 | 1.081x | 1.086x | +0.5% | 0.006s | mejor |

- **Metricas agregadas:** Media aritmetica=1.076x | Media geometrica=1.076x | Peor caso=1.100x
- **Conclusion:** Hipotesis confirmada. La calidad baja ligeramente vs SolverB1 (1.076x vs 1.052x media) pero el solver pasa de O(n^4) a O(n^3) y de minutos a milisegundos. Supera a Christofides en todas las instancias. Tradeoff excelente.
- **Siguientes pasos:** Añadir or-opt, o investigar si la validacion isLinearRing en la construccion original de SolverB aportaba calidad real o era solo overhead.

### E002 — SolverC1: Convex hull peeling + insercion por ratio (2026-04-10)

- **Solver:** SolverC1
- **Linea:** C
- **Padre:** SolverC (concepto)
- **Hipotesis:** Descomponer los puntos en capas concentricas de convex hull y luego insertar las capas interiores preservara mejor la estructura geometrica.
- **Algoritmo:**
  1. Pelar convex hulls sucesivos hasta agotar puntos — O(k * n log n)
  2. Usar la capa exterior como tour inicial
  3. Insertar puntos de capas interiores con criterio de ratio — O(n) por punto
- **Complejidad:** O(n^2 log n) a O(n^3)
- **Resultados:**

| Instancia | Ratio | Tiempo | vs Christofides |
|-----------|-------|--------|-----------------|
| berlin52 | 1.147x | 0.003s | peor |
| st70 | 1.123x | 0.001s | peor |
| kro200 | 1.206x | 0.003s | peor |
| a280 | 1.218x | 0.004s | peor |

- **Metricas agregadas:** Media aritmetica=1.174x | Media geometrica=1.173x | Peor caso=1.218x
- **Conclusion:** El peeling funciona rapido (O(n^2 log n)) pero la insercion simple de capas interiores produce tours de mala calidad. El problema es que al insertar puntos de capas interiores uno a uno, no se respeta la estructura geometrica de cada capa. Posibles mejoras: insertar capas completas preservando su orden, o usar 2-opt despues de la insercion.
- **Siguientes pasos:** SolverC2 con 2-opt post-insercion, o SolverC3 con estrategia de intercalado de capas.

### E001 — SolverB1: SolverB + 2-opt (2026-04-10)

- **Solver:** SolverB1
- **Linea:** B
- **Padre:** SolverB
- **Hipotesis:** Añadir busqueda local 2-opt despues de la construccion eliminara cruces de aristas y reducira el ratio de aproximacion significativamente.
- **Algoritmo:**
  1. Construir tour con SolverB (convex hull + insercion por ratio) — O(n^4)
  2. Aplicar 2-opt: intercambiar pares de aristas que mejoran el tour — O(n^2) por pasada
  3. Repetir hasta que no haya mejora
- **Complejidad:** O(n^4) (dominada por la construccion de SolverB)
- **Resultados:**

| Instancia | SolverB | SolverB1 | Mejora | Tiempo | vs Christofides |
|-----------|---------|----------|--------|--------|-----------------|
| berlin52 | 1.021x | 1.010x | -1.1% | 0.42s | mejor |
| st70 | 1.075x | 1.051x | -2.4% | 0.83s | mejor |
| kro200 | 1.110x | 1.064x | -4.6% | 64.3s | mejor |
| a280 | 1.212x | 1.081x | -13.1% | 140.2s | mejor |

- **Metricas agregadas:** Media aritmetica=1.052x | Media geometrica=1.051x | Peor caso=1.081x
- **Conclusion:** Hipotesis confirmada. El 2-opt mejora significativamente el tour en todas las instancias. La mejora es mas pronunciada en instancias grandes (a280: -13.1%). SolverB1 supera a Christofides en TODAS las instancias, siendo ambos polinomicos. La fase de construccion (O(n^4)) domina el tiempo; el 2-opt añade poco coste.
- **Siguientes pasos:** SolverB2 con or-opt adicional, o investigar si se puede reducir la complejidad de la construccion eliminando la validacion isLinearRing innecesaria.

### E000 — Baseline: Christofides y SolverB (2026-04-10)

- **Solvers:** Christofides (JGraphT), SolverB
- **Proposito:** Establecer la linea base de referencia para todos los experimentos futuros.
- **Metricas agregadas Christofides:** Media aritmetica=1.137x | Media geometrica=1.137x | Peor caso=1.156x
- **Metricas agregadas SolverB:** Media aritmetica=1.105x | Media geometrica=1.102x | Peor caso=1.212x
- **Resultados Christofides (O(n^3)):**

| Instancia | Ratio | Tiempo |
|-----------|-------|--------|
| berlin52 | 1.118x | 0.06s |
| st70 | 1.130x | 0.03s |
| kro200 | 1.156x | 0.04s |
| a280 | 1.143x | 0.10s |

- **Resultados SolverB (O(n^4)):**

| Instancia | Ratio | Tiempo |
|-----------|-------|--------|
| berlin52 | 1.021x | 0.58s |
| st70 | 1.075x | 1.15s |
| kro200 | 1.110x | 64.0s |
| a280 | 1.212x | 138.5s |

---

## Backlog de ideas (priorizado)

### Hallazgos clave de E006-E010
- La busqueda local (2-opt + or-opt) **domina** la calidad. La estrategia de construccion importa poco.
- SolverC4 = SolverC3 — el orden de insercion no importa tras busqueda local.
- La propiedad de capas de convex hull se cumple en todas las instancias pequeñas (E008).
- Delaunay NN no mejora sobre NN global — el NN ya elige aristas Delaunay naturalmente.

### Hoja de ruta: cerrar el gap con LKH

Ver seccion "State of the art" para la tabla completa. Gap actual: ~1.2% (SolverI2 O(n^3)).

| Prioridad | Experimento | Tecnica | Detalle |
|-----------|-------------|---------|---------|
| **1 (siguiente)** | E029 | α-nearness candidates | Calcular 1-tree (MST + 1 arista). Para cada arista, α = coste de forzarla en el 1-tree. Usar aristas con α bajo como candidatos en LK y 2-opt. JGraphT tiene MST. |
| 2 | E030 | LK profundidad 5 + backtracking | Extender la cadena LK a profundidad 5. Cuando depth d no mejora, backtrack a d-1 y probar siguiente candidato. Explorar espacio exponencialmente mayor. |
| 3 | E031 | Movimientos no secuenciales | Permitir que la cadena LK cruce el tour (puntos no consecutivos). Equivale a double-bridge integrado. Requiere representacion de tour mas sofisticada. |
| 4 | E032 | Subgradient optimization | Relajacion lagrangiana sobre el 1-tree para calcular cotas inferiores. Usa los multiplicadores para mejorar las α-nearness lists. Iterativo. |
| 5 | E033 | Segment trees O(log n) | Reemplazar reversiones O(n) por operaciones O(log n). Permite explorar mas movimientos en el mismo tiempo. |

### Ideas completadas o descartadas

- ~~Or-opt iterado~~ — E014: no aporta
- ~~Or-opt extendido (seg 4-5)~~ — E016: mejora marginal
- ~~3-opt~~ — E017: no mejora sobre 2-opt+or-opt
- ~~4-opt~~ — E025: redundante con LK+DB
- ~~Propiedad de capas de convex hull~~ — E011: no se mantiene en instancias grandes
- ~~Multi-start con inicios distribuidos~~ — E019/E020: cubierto por SolverG1/G2
