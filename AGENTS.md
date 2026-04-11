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
