# Log de Investigacion — TSP Euclideo 2D

## Objetivo

Encontrar un algoritmo de complejidad polinomica que resuelva el TSP Euclideo 2D de forma optima.

**Restriccion:** Solo se investigan algoritmos con complejidad polinomica. Algoritmos super-polinomicos (O(2^n), O(n!)) quedan fuera del alcance.

**Señales de progreso:**
- Buena señal: un solver polinomico que aproxima mejor que Christofides (O(n^3), garantia 1.5x)
- Perfecto: un solver polinomico que encuentra la solucion optima

---

## Mejores resultados actuales

| Instancia | Optimo | Mejor Solver | Ratio | Tiempo | Complejidad | Fecha |
|-----------|--------|-------------|-------|--------|-------------|-------|
| berlin52 | 7542.0 | SolverB1 | 1.010x | 0.42s | O(n^4) | 2026-04-10 |
| st70 | 675.0 | SolverB1 | 1.051x | 0.83s | O(n^4) | 2026-04-10 |
| kro200 | 29368.0 | SolverB1 | 1.064x | 64.3s | O(n^4) | 2026-04-10 |
| a280 | 2579.0 | SolverB1 | 1.081x | 140.2s | O(n^4) | 2026-04-10 |

**Nota:** SolverB1 (O(n^4)) supera a Christofides (O(n^3)) en todas las instancias. Primera "buena señal".

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

- **Conclusion:** Hipotesis confirmada. El 2-opt mejora significativamente el tour en todas las instancias. La mejora es mas pronunciada en instancias grandes (a280: -13.1%). SolverB1 supera a Christofides en TODAS las instancias, siendo ambos polinomicos. La fase de construccion (O(n^4)) domina el tiempo; el 2-opt añade poco coste.
- **Siguientes pasos:** SolverB2 con or-opt adicional, o investigar si se puede reducir la complejidad de la construccion eliminando la validacion isLinearRing innecesaria.

### E000 — Baseline: Christofides y SolverB (2026-04-10)

- **Solvers:** Christofides (JGraphT), SolverB
- **Proposito:** Establecer la linea base de referencia para todos los experimentos futuros.
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

1. **SolverB2:** SolverB1 + or-opt (reubicar segmentos de 1-3 puntos) — esperada mejora adicional de 1-3%
2. **Optimizar construccion de SolverB:** Eliminar la validacion isLinearRing en findBestIndexToInsertAt2 (innecesaria si partimos de un poligono convexo), reduciendo de O(n^4) a O(n^2)
3. **SolverC2:** SolverC1 + 2-opt post-insercion — combinar la rapidez del peeling con la mejora local del 2-opt
4. **SolverC3:** Peeling + intercalado de capas preservando orden geometrico — en vez de insertar puntos uno a uno, conectar capas completas
5. **SolverB3:** 3-opt — movimientos mas complejos que 2-opt, puede resolver estructuras que 2-opt no puede
6. **Delaunay-based:** Usar triangulacion de Delaunay para restringir el espacio de busqueda. El tour optimo usa predominantemente aristas de Delaunay.
7. **SolverD:** Investigar si el orden de los vertices dentro de cada capa de convex hull se preserva en el tour optimo (propiedad teorica clave)
