# Euclidean TSP

Biblioteca en Kotlin para resolver el Problema del Viajante (TSP) en el plano euclídeo 2D. Contiene varias implementaciones de algoritmos y un framework de comparación para evaluar su rendimiento.

## Objetivo

Encontrar un algoritmo que resuelva el TSP Euclídeo 2D en tiempo polinómico.

## Estructura del proyecto

- `src/main/kotlin/com/github/dgraciac/euclideantsp/`
  - `shared/` — Tipos base: `Point`, `Tour`, `Euclidean2DTSPInstance`, `Euclidean2DTSPSolver` (interfaz)
  - Solvers por linea de investigacion (~30 solvers, cada uno en su archivo):
    - Linea A: `SolverA` (centroide)
    - Linea B: `SolverB`, `SolverB1`-`SolverB3` (convex hull + insercion)
    - Linea C: `SolverC1`-`SolverC4` (peeling)
    - Linea E: `SolverE1`-`SolverE7` (nearest neighbor + multi-start)
    - Linea F: `SolverF1` (Delaunay)
    - Linea G: `SolverG1`-`SolverG2` (multi-construccion)
    - Linea H: `SolverH1`-`SolverH4` (perturbacion + LK)
    - Linea I: `SolverI1`-`SolverI2` (optimizar dentro de O(n^3))
    - Linea J: `SolverJ1`-`SolverJ6` (busqueda local avanzada: α-nearness, LK deep, subgradient)
  - Utilidades compartidas de busqueda local:
    - `TwoOpt.kt` — 2-opt con safety limit (O(n^3) peor caso)
    - `OrOpt.kt` — or-opt parametrizable (O(n^3) peor caso)
    - `LinKernighan.kt` — LK profundidad 2 con position map
    - `LinKernighanDeep.kt` — LK profundidad 5 con backtracking
    - `DoubleBridge.kt` — perturbacion double-bridge determinista
    - `FourOpt.kt` — 4-opt directo sobre candidatos
    - `LocalSearch.kt` — busqueda local iterativa (ciclo 2-opt/or-opt)
  - Utilidades de candidatos:
    - `NeighborList.kt` — K-nearest y 2-opt acelerado con neighbor lists
    - `AlphaNearness.kt` — α-nearness basado en 1-tree/MST
    - `SubgradientOptimization.kt` — optimizacion Held-Karp para candidatos
  - Construcciones:
    - `Construction.kt` — nearestNeighborFrom, farthestInsertion, convexHullInsertion, peelingInsertion, greedyConstruction
  - Extensiones: `PointExtensions`, `ListExtensions`, `SetExtensions`, `CoordinateExtensions`
  - `jts/` — Funciones auxiliares con JTS (geometria)
- `src/test/kotlin/` — Tests:
  - `ComparisonTest.kt` — Test principal: ejecuta solvers activos en todas las instancias
  - `TSPInstanceProvider.kt` — Provee las instancias TSPLIB para tests parametrizados
  - Instancias TSPLIB: `Eil51`, `Berlin52`, `ST70`, `Eil76`, `Rat99`, `Kro200`, `A280`, `Pcb442`
  - Instancias pequeñas: `Trivial`, `Instance4*`, `Instance5*`, `Instance6*`, `Instance10A`
  - `ConvexHullLayerOrderTest.kt` — Verificacion de propiedad de capas (E008)
  - `ConvexHullLayerOrderLargeTest.kt` — Idem en instancias grandes (E011)
  - `GapAnalysisTest.kt` — Analisis arista por arista del gap (E018)
  - `PassCountAnalysisTest.kt` — Conteo de pasadas de 2-opt/or-opt (E026)
  - `InstanceValidationTest.kt` — Validacion de instancias importadas
- `RESEARCH_LOG.md` — **Fuente principal de contexto para la investigacion**

## Build y tests

```bash
./gradlew build          # Compila, ejecuta tests, spotless check, jacoco report
./gradlew test           # Solo tests
./gradlew spotlessApply  # Formatea código con ktlint
./gradlew spotlessCheck  # Verifica formato
```

- Java 17 / Kotlin 2.3.20
- Gradle 9.4.1 con Kotlin DSL
- Tests con JUnit 6 (parametrizados) y AssertJ

## Convenciones

- Formato de código: ktlint (via Spotless). Ejecutar `./gradlew spotlessApply` antes de commitear.
- Los solvers implementan la interfaz `Euclidean2DTSPSolver` con el método `compute(instance): Tour`.
- Un `Tour` es una lista de `Point` donde el primero y el último son iguales (ciclo cerrado).
- Las instancias de test se definen como `ArgumentsProvider` en `TSPInstanceProvider`.

## Dependencias principales

- **JGraphT** — Grafos y algoritmo de Christofides
- **JTS** (LocationTech) — Geometría computacional

## Protocolo de investigacion

**IMPORTANTE — Al inicio de CADA sesion:**
1. Leer este archivo (`AGENTS.md`) completo para entender el proyecto, las reglas y las convenciones.
2. Leer `RESEARCH_LOG.md` completo para entender el estado actual: mejores resultados, experimentos realizados, backlog de ideas, y state of the art.
3. La investigacion es continua entre sesiones. Todo el contexto necesario esta en el repo. No debe haber ninguna perdida de informacion al cambiar de sesion o de agente. Si descubres algo que solo existe en la conversacion y no en el repo, documentalo ANTES de terminar la sesion.

### Restriccion fundamental

Solo se investigan algoritmos con **complejidad polinomica** (O(n^k) para algun k constante). Algoritmos super-polinomicos (O(2^n), O(n!), etc.) quedan fuera del objetivo. BruteForce existe solo como herramienta de verificacion en tests, no como solver de investigacion ni como referencia de comparacion.

### Criterio de progreso

Cada nuevo solver debe buscar **mejorar la aproximacion Y reducir (o mantener) la complejidad algoritmica**. Restricciones:

- **No combinar solvers:** No se permite "ejecutar dos solvers y quedarse con el mejor" — eso no es un algoritmo mejor, es fuerza bruta sobre algoritmos.
- **No hacer solvers adaptativos:** No se permite que un solver cambie su comportamiento segun n u otras propiedades de la instancia (e.g., "si n<500 haz X, si n>700 haz Y"). Queremos analizar las fortalezas y debilidades de cada enfoque con claridad. Estrategias adaptativas en un mismo solver lo dificultan. Los solvers adaptativos serian para productos comerciales, no para investigacion.
- **Solo O(n^3):** Todos los nuevos solvers deben tener complejidad peor caso O(n^3) hasta agotar todas las posibilidades de mejora en rapidez y calidad dentro de esa cota. No subir a O(n^4) ni superior salvo decision explicita del investigador.
- El objetivo es encontrar UN algoritmo puro que sea mejor, no una cartera ni un meta-algoritmo.

### Metricas de comparacion de solvers

Se registran tres metricas agregadas sobre los ratios de aproximacion de todas las instancias:

1. **Media aritmetica** — Promedio simple de los ratios. Facil de interpretar.
2. **Media geometrica** — Estandar en benchmarking (SPEC, literatura TSPLIB). Penaliza menos outliers y es mas justa con escalas distintas.
3. **Peor caso** — Ratio maximo. Lo mas conservador, relevante si buscamos garantias.

Las tres se registran por solver en `RESEARCH_LOG.md` y en el KDoc de cada solver.

Ademas, para cada solver se debe determinar la **peor aproximacion garantizada** (ratio de aproximacion en el peor caso sobre todas las instancias posibles). Christofides tiene garantia demostrada de 3/2. Nuestros solvers necesitan analisis teorico o contraejemplos para establecer su garantia. Sin esto, la comparacion con Christofides es incompleta: mejor ratio empirico no implica mejor algoritmo si no hay garantia en el peor caso.

### Convencion de nombres de solvers

- **Letra** = linea de investigacion (A = centroide, B = convex hull + ratio, C = peeling, D/E/... = nuevas lineas)
- **Numero** = variante dentro de la linea (SolverB1, SolverB2, ...)
- Cada variante tiene su propio archivo Kotlin: `SolverB1.kt`, `SolverC1.kt`, etc.
- Nuevas lineas de investigacion fundamentalmente diferentes reciben una letra nueva

### Documentacion obligatoria — Funciones de utilidad

Toda funcion de utilidad debe tener KDoc en español con:
- Descripcion de lo que hace
- Parametros y retorno
- **Complejidad en notacion Big O**

### Documentacion obligatoria — Solvers

Todo solver debe tener KDoc en español con:
- Linea de investigacion y solver padre
- Numero de experimento (EXXX)
- Hipotesis que se prueba
- Algoritmo paso a paso
- **Complejidad Big O e2e** (tipica/empirica) **y peor caso** con desglose por paso
- Tabla de resultados (ratio + tiempo por instancia)
- **Metricas agregadas: media aritmetica, media geometrica y peor caso** de los ratios
- Conclusion

### Reglas experimentales

- **Ejecucion en serie:** Los solvers se ejecutan uno tras otro, nunca en paralelo. Esto garantiza que las mediciones de tiempo no se contaminen por compartir CPU entre solvers.
- **Nunca matar tests prematuramente:** Siempre esperar a que terminen. Si un test tarda demasiado, es un resultado en si mismo (indica un bug de rendimiento o un solver demasiado lento). Registrar el tiempo como dato. Si hay sospecha de loop infinito, añadir logging o limites de tiempo en el codigo, no matar el proceso.
- **Complejidad tipica vs peor caso:** Tras E026, la complejidad tipica y peor caso coinciden para todos los solvers. 2-opt converge en <=6 pasadas (O(1)) y or-opt en ~0.2*n pasadas (O(n)), verificado empiricamente en todas las instancias (n=51 a n=442). Safety limits reducidos de n^2 a max(20,n). Resultados identicos. Los safety limits en twoOpt y orOpt garantizan terminacion polinomica.
- **Discrepancia de distancias TSPLIB:** TSPLIB define EUC_2D como `nint(sqrt(dx^2+dy^2))` (redondeada al entero). Nuestro codigo usa distancia euclidea real (sin redondeo). Esto causa que nuestros tours midan ~0.02-0.8% menos que en metrica TSPLIB. Los ratios de aproximacion tienen un pequeño sesgo optimista. Los valores de `optimalLength` en las instancias son los de TSPLIB (calculados con distancias enteras).
- **Validacion de instancias:** `InstanceValidationTest` verifica el numero de puntos, ausencia de duplicados, y coherencia de los optimos declarados. Ejecutar al importar nuevas instancias.

### Protocolo de iteracion

1. **Leer contexto:** `RESEARCH_LOG.md` — mejores resultados, experimentos recientes, backlog de ideas
2. **Formular hipotesis:** Escribir una hipotesis clara y testeable
3. **Implementar:** Crear `SolverXN.kt` con KDoc completo. Reutilizar funciones existentes
4. **Añadir al test:** Una linea en `ComparisonTest.kt` companion object
5. **Ejecutar:** `./gradlew spotlessApply && ./gradlew test`
6. **Registrar:** Rellenar resultados en el KDoc del solver y en `RESEARCH_LOG.md`
7. **Commit:** Formato `experiment(EXXX): SolverXN — descripcion breve`

### Convencion de idioma

- **Codigo** (nombres de variables, funciones, clases): siempre en **ingles**
- **Documentacion** (KDoc, RESEARCH_LOG.md, AGENTS.md, README): siempre en **español**
