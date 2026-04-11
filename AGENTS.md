# Euclidean TSP

Biblioteca en Kotlin para resolver el Problema del Viajante (TSP) en el plano euclídeo 2D. Contiene varias implementaciones de algoritmos y un framework de comparación para evaluar su rendimiento.

## Objetivo

Encontrar un algoritmo que resuelva el TSP Euclídeo 2D en tiempo polinómico.

## Estructura del proyecto

- `src/main/kotlin/com/github/dgraciac/euclideantsp/` — Implementaciones de solvers y utilidades
  - `shared/` — Tipos base: `Point`, `Tour`, `Euclidean2DTSPInstance`, `Euclidean2DTSPSolver` (interfaz)
  - Solvers: `BruteForce`, `Christofides`, `SolverA`, `SolverB`, `SolverC`
  - Extensiones: `PointExtensions`, `ListExtensions`, `SetExtensions`, `CoordinateExtensions`
  - `jts/` — Funciones auxiliares con JTS (geometría)
- `src/test/kotlin/` — Tests parametrizados con instancias TSPLIB (berlin52, st70, kro200, a280) e instancias pequeñas

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

**IMPORTANTE: Leer `RESEARCH_LOG.md` al inicio de cada sesion.** Contiene el estado actual de la investigacion, mejores resultados, experimentos realizados y backlog de ideas.

### Restriccion fundamental

Solo se investigan algoritmos con **complejidad polinomica** (O(n^k) para algun k constante). Algoritmos super-polinomicos (O(2^n), O(n!), etc.) quedan fuera del objetivo. BruteForce existe solo como herramienta de verificacion para instancias pequeñas.

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
- **Complejidad tipica vs peor caso:** La complejidad "e2e" documentada en los solvers es la tipica/empirica. La complejidad "peor caso" tiene en cuenta que 2-opt y or-opt pueden requerir hasta n^2 pasadas antes de converger (safety limit). Sin el safety limit, el numero de mejoras 2-opt puede ser super-polinomico en el peor caso (Englert et al. 2014). Los safety limits en twoOpt y orOpt garantizan terminacion polinomica.
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
