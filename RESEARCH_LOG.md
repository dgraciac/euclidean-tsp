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
esa garantia — su rendimiento solo esta verificado empiricamente en 8 instancias TSPLIB.

---

## Metricas de comparacion

Se registran tres metricas agregadas sobre los ratios de aproximacion de todas las instancias:
1. **Media aritmetica** — Promedio simple de los ratios
2. **Media geometrica** — Estandar en benchmarking, penaliza menos outliers
3. **Peor caso** — Ratio maximo, relevante para garantias

## Mejores resultados actuales

15 instancias TSPLIB con optimo demostrado (n=51 a n=2103):

| Instancia | n | Optimo | Mejor O(n^3) | Ratio | Christofides |
|-----------|---|--------|-------------|-------|-------------|
| eil51 | 51 | 426 | SolverJ5 | 1.007x | 1.137x |
| berlin52 | 52 | 7542 | SolverJ5 | 1.000x | 1.118x |
| st70 | 70 | 675 | SolverJ5 | 1.020x | 1.135x |
| eil76 | 76 | 538 | SolverJ5 | 1.025x | 1.161x |
| rat99 | 99 | 1211 | SolverJ5 | 1.007x | 1.139x |
| kro200 | 200 | 29368 | SolverJ5 | 1.004x | 1.148x |
| a280 | 279 | 2579 | SolverJ5 | 1.006x | 1.152x |
| pcb442 | 442 | 50778 | SolverJ5 | 1.013x | 1.117x |
| d657 | 657 | 48912 | SolverL1 | 1.030x | 1.129x |
| rat783 | 783 | 8806 | SolverL1 | 1.021x | 1.154x |
| pr1002 | 1002 | 259045 | SolverJ5 | 1.026x | 1.118x |
| u1060 | 1060 | 224094 | SolverJ5 | 1.042x | ~1.13x |
| d1291 | 1291 | 50801 | SolverL1 | 1.038x | 1.144x |
| fl1577 | 1577 | 22249 | SolverJ5 | 1.014x | ~1.13x |
| d2103 | 2103 | 80450 | SolverL1 | 1.011x | 1.045x |

### Resumen agregado por solver

| Solver | Complejidad | Media arit. | Media geom. | Peor caso | Tiempo max |
|--------|-------------|------------|------------|-----------|------------|
Total de experimentos documentados: E000-E043 (43 experimentos).
Nota: metricas agregadas de la tabla siguiente calculadas sobre instancias disponibles en el momento de cada solver. Consultar KDoc de cada solver para metricas exactas.

| Solver | Tipica | Peor caso | Media geom. | Peor ratio | Tiempo max |
|--------|--------|-----------|------------|------------|------------|
| **SolverG2** | **O(n^4)** | **O(n^4)** | **1.012x** | **1.021x** | **84s** |
| SolverE2/E4 | O(n^4) | O(n^4) | 1.012x | 1.027x | 55s |
| SolverH1 | O(n^3.5) | O(n^3.5) | 1.015x | 1.028x | 3.0s |
| SolverE3 | O(n^3.5) | O(n^3.5) | 1.018x | 1.034x | 1.1s |
| SolverG1 | O(n^3.5) | O(n^3.5) | 1.015x | 1.034x | 2.0s |
| SolverE7 | O(n^4) | O(n^4) | 1.011x | 1.021x | 83s |
| ~~SolverK2~~ | O(n^3) | O(n^3) | 1.015x | 1.038x | 469s | (descartado: combina solvers)
| **SolverJ5** | **O(n^3)** | **O(n^3)** | **1.010x** | **1.025x** | **3.99s** | (mejor calidad O(n^3))
| **SolverL2** | **O(n^3)** | **O(n^3)** | **~1.018x** | **~1.047x** | **194s** | (J5 + DB rapido, 1.5-4x mas rapido)
| **SolverL3** | **O(n^3)** | **O(n^3)** | **~1.019x** | **~1.061x** | **61s** | (L2 sin LK-deep, 3-7x mas rapido que J5)
| **SolverM2** | **O(n^2)** | **O(n^2)** | **~1.007x** | **~1.038x** | **1891s** | (J5 NL + alpha lean, escala a n=5915)
| SolverN1 | O(n^2) | O(n^2) | ~1.028x | ~1.080x | 2410s | (insercion geometrica Delaunay, linea N pausada)
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

## State of the art — Algoritmos polinomicos para TSP Euclideo 2D

Solo se compara con algoritmos de complejidad polinomica garantizada.
Algoritmos no polinomicos quedan fuera del alcance del proyecto.

### Algoritmos con garantia teorica (de la literatura)

| Algoritmo | Año | Complejidad peor caso | Garantia aprox. | Practico? |
|-----------|-----|----------------------|-----------------|-----------|
| Christofides | 1976 | O(n^3) | 3/2 | Si |
| Arora PTAS | 1996 | O(n (log n)^O(1/ε)) | 1+ε para todo ε>0 | No (*) |
| Rao-Smith PTAS | 1998 | O(2^(1/ε)^O(1) * n log n) | 1+ε para todo ε>0 | No (*) |
| Bartal et al. | 2013 | O(2^(1/ε)^O(1) * n) | 1+ε para todo ε>0 | No (*) |
| Karlin-Klein-Gharan | 2021 | O(n^3) | 3/2 - 10^-36 (metrico) | Si |

(*) Los PTAS son polinomicos para ε fijo pero las constantes O(1/ε) los hacen impracticos
para ε pequeño. Para ε=0.01 (1% gap), la constante 2^(1/0.01)^O(1) es astronomica.

**Nota sobre 2-opt:** El numero de mejoras 2-opt en el peor caso es exponencial incluso para
instancias euclideas (Englert, Roeglin, Voecking 2007). Sin embargo, bajo modelos probabilisticos
(instancias perturbadas), el numero es polinomico. Nuestro safety limit (max n pasadas, E026)
garantiza terminacion polinomica. Empiricamente converge en <=6 pasadas.

**Nota sobre ratio de aproximacion de 2-opt:** El peor caso del ratio de aproximacion de un
tour 2-opt local en instancias euclideas es Theta(log n / log log n) (Chandra, Karloff, Tovey 1999).
Esto significa que 2-opt SIN multi-start no tiene garantia constante.

### Nuestros mejores solvers vs el state of the art

| Algoritmo | Complejidad peor caso | Gap empirico (8 inst.) | Garantia aprox. |
|-----------|----------------------|----------------------|-----------------|
| **Nuestro SolverJ5** | **O(n^3)** | **~1.0%** | **Desconocida** |
| Nuestro SolverH3 | O(n^4) | ~0.6% | Desconocida |
| Christofides | O(n^3) | ~15% | 3/2 demostrada |
| Arora PTAS (ε=0.01) | O(n (log n)^O(100)) | 1% (teorico) | 1.01 demostrada |

SolverJ5 y Christofides tienen la misma complejidad peor caso O(n^3).
SolverJ5 tiene gap empirico ~15x menor (1.0% vs ~15%).
El PTAS de Arora garantiza 1+ε pero es impractico para ε pequeño.

**Preguntas abiertas para nuestro proyecto:**
1. Cual es la garantia de aproximacion de SolverJ5 en el peor caso?
2. Existe un algoritmo O(n^3) con garantia demostrada mejor que 3/2 para TSP euclideo 2D?
3. Puede nuestro enfoque (multi-start + busqueda local con safety limits) tener garantia sublineal?

### E033 — Busqueda empirica de garantia de aproximacion (2026-04-11)

**Resultado:** En todas las instancias adversariales pequeñas (n=4 a n=12), SolverJ5
encuentra el tour **optimo exacto** (ratio 1.000000). Ningun contraejemplo encontrado.

| Tipo instancia | n | SolverJ5 ratio | Christofides ratio |
|----------------|---|---------------|-------------------|
| Grids (7 instancias) | 4-12 | 1.000x en todas | hasta 1.250x |
| Clusters (10 instancias) | 6-8 | 1.000x en todas | hasta 1.000x |
| Zigzag (16 instancias) | 6-10 | 1.000x en todas | hasta 1.145x |
| Estrellas (5 instancias) | 6-10 | 1.000x en todas | hasta 1.149x |
| TSPLIB (8 instancias) | 51-442 | hasta 1.025x | hasta 1.144x |

**Peor ratio encontrado de SolverJ5:** 1.025x (eil76, n=76)
**Peor ratio encontrado de Christofides:** 1.144x (eil76, n=76)

**Limitaciones del analisis:**
- Instancias adversariales limitadas a n<=12 (BruteForce)
- No se han construido las instancias especificas de Chandra-Karloff-Tovey (1999)
  que prueban el peor caso teorico Theta(log n/log log n) de 2-opt
- El ratio podria crecer con n — necesitamos instancias de n=1000+ para verificar
- Esto es evidencia empirica, NO una demostracion teorica

**Conclusion:** SolverJ5 domina empiricamente a Christofides en todas las instancias
probadas (8 TSPLIB + ~38 adversariales). Pero sin demostracion teorica, no podemos
afirmar que su garantia de aproximacion sea mejor que 3/2.

### Tecnicas integradas de la literatura de busqueda local

1. α-nearness candidates basados en 1-tree (E029) — integrado
2. Lin-Kernighan profundidad variable con backtracking (E030) — integrado
3. Subgradient optimization (E032) — probado, sin mejora consistente
4. Movimientos no secuenciales — pendiente
5. Segment trees O(log n) — pendiente

### Fuentes

- Arora (1996): "Polynomial Time Approximation Schemes for Euclidean TSP" — PTAS original
- Rao, Smith (1998): Mejora del PTAS a O(n log n)
- Bartal et al. (2013): PTAS en tiempo lineal O(n)
- Christofides (1976): Algoritmo 3/2-aproximacion
- Karlin, Klein, Oveis Gharan (2021): Primera mejora sobre 3/2 para TSP metrico general
- Englert, Roeglin, Voecking (2007): Numero exponencial de mejoras 2-opt en peor caso
- Chandra, Karloff, Tovey (1999): Ratio de aproximacion de 2-opt local es Theta(log n/log log n)

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

### E055 — SolverN1: Insercion geometrica desde convex hull guiada por Delaunay (2026-04-13)

- **Solver:** SolverN1
- **Linea:** N (insercion geometrica — nueva linea de investigacion)
- **Complejidad:** O(n^2)
- **Hipotesis:** Usando la triangulacion de Delaunay como mapa de vecindad natural, podemos
  determinar geometricamente donde insertar cada punto interior sin probar todas las aristas
  del tour. Para cada punto P, sus vecinos Delaunay que ya estan en el tour indican la posicion.
- **Algoritmo:** Delaunay + convex hull inicial + insercion por vecindad Delaunay (nearest-to-boundary
  order) + 2-opt-nl + or-opt-nl + LK(2) + DB-nl + LK(2). Sin multi-start, sin ramas duales.
- **Resultados:**

  | Instancia | n | Ratio | Tiempo |
  |---|---|---|---|
  | eil51 | 51 | 1.025x | 0.2s |
  | berlin52 | 52 | 1.050x | 0.1s |
  | st70 | 70 | 1.023x | 0.1s |
  | eil76 | 76 | 1.036x | 0.1s |
  | rat99 | 99 | 1.008x | 0.1s |
  | kro200 | 200 | 1.017x | 0.6s |
  | a280 | 279 | 1.041x | 0.4s |
  | pcb442 | 442 | 1.047x | 0.6s |
  | d657 | 657 | 1.043x | 10.0s |
  | pr1002 | 1002 | 1.059x | 21.9s |
  | d2103 | 2103 | 1.059x | 92.3s |
  | pcb3038 | 3038 | 1.052x | 465.7s |
  | rl5915 | 5915 | 1.080x | 2409.7s |

- **Metricas (8 instancias clasicas, n=51-442):** Media aritmetica=1.028x | Peor caso=1.050x
- **Conclusion:** Primer solver de linea N. Calidad inferior a J5 (~3-5% gap vs ~1%) pero
  estructura interesante: la insercion Delaunay da buenas semillas sin multi-start. La calidad
  se degrada con n (1.08x en n=5915). Velocidad buena en instancias pequenas (0.6s en pcb442
  vs 5.1s de J5) pero escala mal por LK + DB. **Linea pausada, pendiente de continuar.**

### E054 — SolverM3: Sin alpha-nearness, solo dist-NL K=15 (2026-04-13)

- **Solver:** SolverM3
- **Linea:** M (escalabilidad sub-cubica)
- **Complejidad:** O(n^2 log n)
- **Hipotesis:** Eliminar alpha-nearness (mayor coste de infraestructura) y usar solo
  distance-based NL con K=15 sera mas rapido manteniendo calidad.
- **Resultados:**

  | Instancia | n | Ratio | Tiempo |
  |---|---|---|---|
  | pcb442 | 442 | 1.024x | 3.3s |
  | pr1002 | 1002 | 1.032x | 57.0s |
  | d2103 | 2103 | 1.012x | 659.0s |
  | pcb3038 | 3038 | 1.031x | 465.2s |
  | rl5915+ | — | No ejecutado | LK-deep K=15 inviable a esta escala |

- **Conclusion:** **Paso atras.** Sin alpha-nearness pierde calidad en pcb442 (1.024x vs 1.007x de M2).
  Mas lento que M2 en pr1002 (57s vs 23s) y d2103 (659s vs no probado). LK-deep con K=15 explota
  a n>5000 (>7h sin terminar). Alpha-nearness NO era el cuello de botella — LK-deep lo es.

### E053 — SolverM2: M1 + infraestructura lean (alpha sin JGraphT, hull limitado) (2026-04-12)

- **Solver:** SolverM2
- **Linea:** M (escalabilidad sub-cubica)
- **Complejidad:** O(n^2)
- **Hipotesis:** Los cuellos de botella de M1 son la infraestructura (alpha-nearness con JGraphT
  causa OOM, multi-start sin limite). Optimizando estos, M2 escala a n>5000.
- **Resultados:**

  | Instancia | n | Ratio | Tiempo |
  |---|---|---|---|
  | pcb442 | 442 | 1.007x | 2.2s |
  | pr1002 | 1002 | 1.027x | 22.9s |
  | pcb3038 | 3038 | 1.038x | 403.6s |
  | rl5915 | 5915 | 1.032x | 1891.3s |

- **Metricas:** Mejor solver M. Escala a n=5915 sin OOM (J5 daba OOM en n=11849).
- **Conclusion:** **Mejor solver de linea M.** Calidad comparable a J5, elimina OOM gracias a
  alpha-nearness lean. Pero 1891s en n=5915 sigue muy lejos del objetivo <60s. El cuello de
  botella es LK-deep, no la infraestructura.

### E052 — SolverM1: J5 con todas las fases NL (sub-cubico) (2026-04-12)

- **Solver:** SolverM1
- **Linea:** M (escalabilidad sub-cubica — nueva linea de investigacion)
- **Complejidad:** O(n^2 log n)
- **Hipotesis:** Reemplazar todas las fases O(n^3) de J5 (2-opt completo, or-opt completo,
  double-bridge con re-optimizacion completa) por versiones aceleradas con neighbor lists
  reduce la complejidad a O(n^2 log n) sin perder calidad.
- **Cambios:** orOpt -> orOptWithNeighborLists, twoOpt -> twoOptWithNeighborLists,
  doubleBridgePerturbation -> doubleBridgePerturbationNl.
- **Resultados (comparacion directa con J5):**

  | Instancia | n | J5 ratio/tiempo | M1 ratio/tiempo | Speedup |
  |---|---|---|---|---|
  | eil51 | 51 | 1.007x/0.2s | 1.015x/0.1s | 2.85x |
  | berlin52 | 52 | 1.000x/0.1s | 1.000x/0.2s | 0.58x |
  | st70 | 70 | 1.020x/0.3s | 1.017x/0.2s | 1.15x |
  | eil76 | 76 | 1.025x/0.2s | 1.031x/0.1s | 4.19x |
  | rat99 | 99 | 1.007x/0.1s | 1.008x/0.1s | 1.87x |
  | kro200 | 200 | 1.004x/1.2s | 1.004x/0.7s | 1.69x |
  | a280 | 279 | 1.006x/1.3s | 1.008x/0.9s | 1.34x |
  | pcb442 | 442 | 1.013x/5.1s | 1.007x/1.6s | 3.27x |
  | d657 | 657 | 1.037x/22.9s | 1.038x/15.0s | 1.53x |
  | pr1002 | 1002 | 1.026x/37.2s | 1.027x/25.3s | 1.47x |
  | d2103 | 2103 | 1.014x/239.4s | 1.011x/202.8s | 1.18x |

  M1 en instancias grandes: pcb3038 1.038x/408s (J5: 651s).

- **Conclusion:** Calidad practicamente identica a J5 (+-0.005). Speedup 1.2-3.3x. El speedup
  disminuye con n, lo que indica que las fases cubicas NO dominaban — la infraestructura
  (alpha-nearness, neighbor lists, multi-start) es el verdadero cuello de botella.

### Escalabilidad de SolverJ5 a escala industrial (2026-04-12)

- **Contexto:** Fase 0.1 del plan de comercializacion. Importadas 4 instancias TSPLIB nuevas:
  pcb3038 (n=3038, PCB drilling real), rl5915 (n=5915), rl11849 (n=11849), usa13509 (n=13509).
- **Infraestructura:** Ficheros .tsp en resources + parser TspLibParser.kt (lazy loading).
  Instancias >~8000 puntos no pueden inlinearse en Kotlin (JVM method size limit 64KB).
- **Resultados de SolverJ5:**

  | Instancia | n | Ratio | Tiempo | Viable (<2%, <60s)? |
  |---|---|---|---|---|
  | pcb442 | 442 | 1.013x | 4.8s | SI |
  | d657 | 657 | 1.037x | 21.2s | MARGINAL |
  | pr1002 | 1002 | 1.026x | 31.3s | MARGINAL |
  | d2103 | 2103 | 1.014x | 251.4s | NO |
  | pcb3038 | 3038 | 1.034x | 651s | NO |
  | rl5915 | 5915 | 1.026x | 3687s | NO |
  | rl11849 | 11849 | OOM | — | NO |

- **Conclusion:** **Resultado BLOQUEANTE para comercializacion.** Calidad buena (1-3.7% gap) a
  cualquier escala, pero tiempo inaceptable para n>1000. SolverJ5 solo es comercialmente viable
  para n<500 (~5s). OOM en n>10000 por alpha-nearness con JGraphT.

### E039 — SolverK2: Mejor de J5 y K1 (2026-04-11)

- **Solver:** SolverK2
- **Linea:** K
- **Complejidad:** O(n^3)
- **Resultados:** Mejor que J5 y K1 en todas las instancias simultaneamente.
  Records: st70 1.003x, rat783 1.026x, d1291 1.038x.
- **Metricas:** Media=1.015x (13 instancias, n=51 a n=2103) | Peor caso=1.038x
- **Conclusion:** Combinar J5+K1 es simple y efectivo. Cada uno gana en su rango de n.

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
- **Linea:** J (busqueda local avanzada)
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

### Hoja de ruta — Estado (todas completadas o descartadas)

| Tecnica | Experimento | Estado | Resultado |
|---------|-------------|--------|-----------|
| α-nearness (1-tree) | E029 | Completado | Mejora en a280/pcb442, integrado en SolverJ5 |
| LK profundidad 5 + backtracking | E030 | Completado | Mejora en kro200, integrado |
| Subgradient optimization | E032 | Completado | Sin mejora consistente |
| Movimientos no secuenciales | E036 | Completado | Sin mejora, DB ya lo cubre |
| Segment trees O(log n) | — | Pendiente | Solo velocidad, no calidad |
| Garantia de aproximacion | E033 | Parcial | Sin contraejemplo en ~46 instancias |
| Instancias adversariales n=1000 | E034-E035 | Completado | J5 gana o iguala a Christofides en todas |
| Instancias TSPLIB n>500 | E037 | En progreso | d657 (n=657, opt=48912), rat783 (n=783, opt=8806) importadas. Test corriendo pero puede no terminar en esta sesion. Re-ejecutar: `./gradlew test --tests "*.ComparisonTest"`. SolverH3 es MUY lento en estas instancias (~30+ min) — considerar ejecutar solo J5 y Christofides. |

### Ideas completadas o descartadas

- ~~Or-opt iterado~~ — E014: no aporta
- ~~Or-opt extendido (seg 4-5)~~ — E016: mejora marginal
- ~~3-opt~~ — E017: no mejora sobre 2-opt+or-opt
- ~~4-opt~~ — E025: redundante con LK+DB
- ~~Propiedad de capas de convex hull~~ — E011: no se mantiene en instancias grandes
- ~~Multi-start con inicios distribuidos~~ — E019/E020: cubierto por SolverG1/G2
- ~~Movimientos no secuenciales en LK~~ — E036: identico a J5, DB ya lo cubre
- ~~Subgradient optimization~~ — E032: sin mejora consistente

### Ideas pendientes

#### Resumen de mejores solvers actuales
- **J5** (O(n^3)): mejor calidad (media ~1.010x), lento (251s en d2103), OOM en n>10000
- **M2** (O(n^2)): calidad ~J5, escala a n=5915 sin OOM, pero 1891s en n=5915
- **L2** (O(n^3)): misma calidad que J5, 1.5-4x mas rapido (DB dos fases)
- **L3** (O(n^3)): -0.3% calidad, 3-7x mas rapido que J5 (sin LK-deep)
- **N1** (O(n^2)): calidad inferior (~3-5% gap), rapido en n<500, primer solver de linea N
- **Christofides** (O(n^3)): garantia 3/2, media ~1.137x

#### Hallazgos clave de E052-E055 (lineas M y N)

- **LK-deep es el cuello de botella real a escala grande**, no alpha-nearness ni 2-opt/or-opt.
  M1 acelero 2-opt/or-opt/DB a O(n^2) pero el speedup fue solo 1.2-3.3x. LK-deep con K=15
  a n>5000 es inviable (>7h sin terminar en M3).
- **Alpha-nearness lean (sin JGraphT) elimina OOM** a n>10000. buildAlphaNearnessListLean
  usa O(n) memoria vs O(n^2) objetos Java de la version original.
- **orOptWithNeighborLists funciona** a escala grande (contrariamente a E046 que fue inconsistente
  en instancias medianas). A n>1000 la aceleracion NL domina sobre el overhead de position map.
- **Sin alpha-nearness la calidad empeora** (M3 vs M2: pcb442 1.024x vs 1.007x). Alpha-nearness
  es critico para la calidad de los candidatos.
- **Insercion geometrica Delaunay** (N1) da semillas razonables sin multi-start. La triangulacion
  de Delaunay identifica correctamente la posicion de insercion para la mayoria de puntos.

#### Mejorar rapidez sin perder calidad
1. ~~Precomputar matriz de distancias~~ — E044: mejora marginal.
2. ~~Reducir candidatos K=7+7 a K=5+5~~ — E045: PIERDE CALIDAD.
3. ~~Or-opt con neighbor lists~~ — E046: inconsistente en medianas. **Funciona en grandes (E052).**
4. ~~Refactorizar pipeline para DistanceMatrix~~ — E047: MAS LENTO en n>200.
5. ~~Eliminar alpha-nearness~~ — E054 (M3): PIERDE CALIDAD y es mas lento. Descartado.
6. **Reemplazar LK-deep por algo mas rapido sin perder calidad** — LK-deep(5) con K=14 es el
   cuello de botella a escala grande. Opciones:
   a. LK-deep con profundidad reducida (3 en vez de 5)
   b. LK-deep con K reducido solo para la fase deep (K=7 deep, K=14 shallow)
   c. Eliminar LK-deep completamente y compensar con mas intentos de DB
   d. LK-deep solo en la rama B, no en el post-DB
7. **KD-tree para buildNeighborLists** — Reducir O(n^2 log n) a O(n log n * K). Mejora la
   infraestructura sin tocar la calidad.

#### Mejorar calidad sin perder rapidez
8. **Mas intentos de DB con el tiempo ahorrado por L2/L3** — Seria un L3 con mas DB.
9. **Mejores candidatos para DB** — Usar α-nearness para identificar aristas "fuera de lugar".
10. **LK con mas candidatos (K=20) solo en la fase final**.

#### Linea N — Insercion geometrica (pausada, pendiente de continuar)
11. **N2: Mejorar criterio geometrico** — Combinar Delaunay con angulo de vision (inscribed angle)
    para desambiguar cuando no hay par Delaunay adyacente en el tour.
12. **N3: Multi-start geometrico** — Varias construcciones Delaunay con diferentes ordenes de
    insercion (por capas, por distancia, por angulo desde centroide).
13. **N+M: Combinar construccion N con busqueda local M** — Usar la semilla geometrica de N1
    con el pipeline NL de M2 como alternativa al multi-start.

#### Investigacion fundamental
14. **Garantia teorica** — Demostrar cota de aproximacion o encontrar contraejemplo grande.
15. **PTAS de Arora** — Unica aproximacion polinomica con garantia (1+ε) demostrada.

#### Comercializacion
16. **Ver COMMERCIALIZATION_PLAN.md** — Plan de licenciamiento como SDK. Fase 0.1 completada
    con resultado bloqueante: el solver no escala a escala industrial (n>1000 en <60s).
    El cuello de botella es LK-deep. Resolver punto 6 de este backlog desbloquea la via comercial.

### Ideas completadas o descartadas

- ~~Solver adaptativo~~ — Descartado por regla de investigacion (dificulta analisis)
- ~~Combinar solvers~~ — Descartado por regla de investigacion (no es un algoritmo mejor)
- ~~Mejorar DB~~ — E041: DoubleBridgeFast (dos fases), integrado en L2
- ~~Eliminar LK-deep~~ — E043: L3, 3-7x mas rapido, -0.3% calidad
- ~~Escalabilidad L2~~ — E042: confirmada hasta n=2103
- ~~Or-opt iterado~~ — E014: no aporta
- ~~Or-opt extendido (seg 4-5)~~ — E016: mejora marginal
- ~~3-opt~~ — E017: no mejora sobre 2-opt+or-opt
- ~~4-opt~~ — E025: redundante con LK+DB
- ~~Propiedad de capas de convex hull~~ — E011: no se mantiene en instancias grandes
- ~~Multi-start con inicios distribuidos~~ — E019/E020: cubierto por SolverG1/G2
- ~~Movimientos no secuenciales en LK~~ — E036: identico a J5, DB ya lo cubre
- ~~Subgradient optimization~~ — E032: sin mejora consistente
- ~~Segment trees~~ — E038: cuello de botella es DB, no reversiones
- ~~Instancias TSPLIB 1000+~~ — E037/E042: completado hasta n=2103
- ~~Instancias TSPLIB 5000+~~ — E052: completado. pcb3038, rl5915, rl11849, usa13509 importadas
- ~~NL everywhere (M1)~~ — E052: funciona, speedup modesto 1.2-3.3x
- ~~Alpha-nearness lean (M2)~~ — E053: elimina OOM, mejor solver M
- ~~Sin alpha-nearness (M3)~~ — E054: paso atras, pierde calidad y velocidad
- ~~Insercion geometrica Delaunay (N1)~~ — E055: primer solver linea N, calidad inferior, pausada
