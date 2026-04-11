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

| Instancia | Optimo | Mejor Solver | Ratio | Tiempo | Complejidad | Fecha |
|-----------|--------|-------------|-------|--------|-------------|-------|
| berlin52 | 7542.0 | SolverC3/C4 | 1.000x | 0.010s | O(n^3) | 2026-04-10 |
| st70 | 675.0 | SolverB3 | 1.020x | 0.003s | O(n^3) | 2026-04-11 |
| kro200 | 29368.0 | SolverE1 | 1.016x | 0.009s | O(n^3) | 2026-04-11 |
| a280 | 2579.0 | SolverE1 | 1.050x | 0.061s | O(n^3) | 2026-04-11 |

### Resumen agregado por solver

| Solver | Complejidad | Media arit. | Media geom. | Peor caso | Tiempo max |
|--------|-------------|------------|------------|-----------|------------|
| **SolverE1** | **O(n^3)** | **1.041x** | **1.041x** | **1.053x** | **0.061s** |
| SolverC3 | O(n^3) | 1.037x | 1.036x | 1.069x | 0.031s |
| SolverC4 | O(n^3) | 1.037x | 1.036x | 1.069x | 0.085s |
| SolverB3 | O(n^3) | 1.040x | 1.039x | 1.055x | 0.064s |
| SolverB1 | O(n^4) | 1.052x | 1.051x | 1.081x | 140.2s |
| SolverF1 | O(n^3) | 1.058x | 1.057x | 1.065x | 0.054s |
| SolverB2 | O(n^3) | 1.076x | 1.076x | 1.100x | 0.006s |
| SolverC2 | O(n^3) | 1.070x | 1.069x | 1.087x | 0.010s |
| Christofides | O(n^3) | 1.137x | 1.137x | 1.156x | 0.10s |
| SolverC1 | O(n^3) | 1.174x | 1.173x | 1.218x | 0.004s |

**Notas:**
- SolverC3 tiene la mejor media (1.036x) pero SolverE1 tiene mejor peor caso (1.053x vs 1.069x).
- SolverC3/C4 son identicos — el orden de insercion de capas interiores no importa tras busqueda local.
- SolverE1 (nearest neighbor) gana en kro200 (1.016x) y a280 (1.050x) — la busqueda local domina.
- La propiedad de capas de convex hull se preserva en todas las instancias pequeñas (E008).
- Berlin52: SolverC3/C4 alcanzan ratio 1.0003 — practicamente optimo.

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

### Ideas pendientes
1. **Mejorar la busqueda local** (mayor impacto dado que domina la calidad):
   a. **Or-opt iterado con 2-opt:** Alternar 2-opt y or-opt multiples veces hasta convergencia global
   b. **Delaunay-restricted 2-opt:** Limitar los intercambios de 2-opt a aristas cercanas (vecinos Delaunay) para acelerar sin perder calidad
   c. **3-opt selectivo:** Aplicar 3-opt solo a segmentos del tour que no mejoran con 2-opt/or-opt
2. **Explotar la propiedad de capas (E008):**
   a. Verificar en instancias mas grandes (necesitaria un solver exacto o tours optimos conocidos)
   b. Si se confirma: construir un solver que fije el orden de cada capa y solo busque el intercalado optimo
   c. Formalizar como teorema y buscar demostracion
3. **Multi-start:** Ejecutar el mismo pipeline desde multiples puntos iniciales de NN y quedarse con el mejor tour. Trivialmente paralelizable, no cambia la complejidad por ejecucion.
4. **Investigar por que berlin52 da casi-optimo:** Entender que estructura geometrica la hace "facil" para nuestro pipeline.
