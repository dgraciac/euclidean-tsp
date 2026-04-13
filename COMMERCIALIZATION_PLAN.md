# Plan de Comercializacion: Licenciamiento como Biblioteca/SDK

Fecha de inicio: 2026-04-12
Via: Licenciamiento a fabricantes de maquinas industriales
Mercado objetivo: Fabricantes de taladradoras de PCB (primera vertical)
Ingresos estimados: $500K-$5M/ano

---

## Resumen ejecutivo

Licenciar el solver TSP euclideo 2D como biblioteca/SDK a fabricantes de maquinas industriales, empezando por taladradoras de PCB (la aplicacion mas pura del TSP euclideo 2D). La propuesta de valor es: calidad competitiva (~1% gap) + tiempo de ejecucion garantizado O(n^3), algo que ningun solver top ofrece.

---

## Fases del plan

### Fase 0: Preparacion tecnica
**Objetivo:** Tener un solver que funcione a escala industrial y este benchmarkeado formalmente.
**Estado:** [X] En progreso — 0.1 completada con resultado BLOQUEANTE

- [X] **0.1 Escalar a n=10,000+** — COMPLETADA 2026-04-12
  - Importadas 4 instancias TSPLIB nuevas: pcb3038, rl5915, rl11849, usa13509.
  - Infraestructura: ficheros .tsp en resources + parser TspLibParser.kt (lazy loading).
  - **Resultado: BLOQUEANTE — el tiempo de ejecucion NO escala a escala industrial.**
  - Criterio de exito original: <2% gap en n=10,000 en <60s. **NO CUMPLIDO.**

  **Resultados de SolverJ5 (2026-04-12):**

  | Instancia | n | Ratio | Tiempo | Viable (<2%, <60s)? |
  |---|---|---|---|---|
  | pcb442 | 442 | 1.0132x | 4.8s | SI |
  | d657 | 657 | 1.0368x | 21.2s | MARGINAL (tiempo) |
  | pr1002 | 1,002 | 1.0257x | 31.3s | MARGINAL (tiempo) |
  | d2103 | 2,103 | 1.0140x | 251.4s | NO (tiempo) |
  | pcb3038 | 3,038 | 1.0341x | 651s | NO (tiempo) |
  | rl5915 | 5,915 | 1.0262x | 3,687s | NO (tiempo) |
  | rl11849 | 11,849 | OOM | — | NO (memoria) |

  **Analisis:**
  - **Calidad: BUENA.** El gap se mantiene en 1-3.7% a cualquier escala. No hay degradacion.
  - **Tiempo: INACEPTABLE para n>1,000.** O(n^3) crece cubicamente: de 4.8s en n=442 a 651s en n=3,038.
  - **Memoria: INSUFICIENTE para n>10,000.** OOM con 4GB heap en n=11,849.
  - **Conclusion:** SolverJ5 tal cual solo es comercialmente viable para n<500 (~5s).
    Esto cubre wire bonding (50-500 pads) pero NO taladrado de PCB (500-300,000 agujeros).

  **Opciones investigadas (E052-E063, 2026-04-13):**
  1. ~~**Optimizar constante**~~ — Probado en 12 variantes (M1-M11). Unico cambio sin perdida
     de calidad: M5 (LK-deep K=7, 1.5x speedup). Insuficiente para n>3,000.
  2. **Particionamiento:** No investigado aun. Rompe pureza pero es solucion industrial estandar.
  3. ~~**Algoritmo sub-cubico**~~ — M1/M2 son O(n^2) teorico pero LK-deep tiene factor K^5=537,824
     que hace que en practica sea ~O(n^2.5). Eliminar componentes O(n^2) individuales no ayuda
     (hay multiples: Prim, DFS, alpha-pairs, multi-start NN, LK-deep). Se necesitaria rediseno
     completo del pipeline.
  4. **Cambiar mercado objetivo:** Viable. Wire bonding (n<500) funciona hoy con M5 (~1.2s).

  **Mejor solver actual para uso practico: SolverM5** (M2 + LK-deep K=7 alpha-only).
  - Calidad identica a J5/M2 (media ~1.011x, peor 1.031x)
  - 1.5x mas rapido que M2: 1.2s en pcb442, 16s en pr1002
  - Sin perdida de calidad vs M2 en ninguna instancia

- [ ] **0.2 Benchmarking formal contra competidores**
  - Comparar contra LKH-3, Google OR-Tools, y 2-opt/or-opt estandar.
  - Medir: calidad (gap vs optimo), tiempo de ejecucion, variabilidad del tiempo.
  - Usar instancias TSPLIB completas (hasta pcb3038 y mayores si existen).
  - Documentar resultados en tabla comparativa.
  - Criterio de exito: datos cuantitativos claros de ventaja en al menos un eje (calidad, tiempo, o predictibilidad).

- [ ] **0.3 Analisis de garantia teorica**
  - Intentar demostrar una cota de aproximacion formal para el solver.
  - Alternativamente, buscar contraejemplos sistematicamente para establecer la peor garantia empirica.
  - Criterio de exito: cota formal demostrada, o peor caso empirico documentado en >1000 instancias aleatorias.

### Fase 1: Productizacion
**Objetivo:** Convertir la biblioteca de investigacion en un producto integrable.
**Estado:** [ ] No iniciada

- [ ] **1.1 API limpia y documentada**
  - Definir API publica minima: input (lista de puntos 2D) -> output (tour ordenado + distancia).
  - Documentacion en ingles.
  - Soporte para Java/Kotlin (nativo) y posiblemente C/C++ (JNI o reimplementacion) para integracion con maquinas industriales.
  - Criterio de exito: API usable por un desarrollador externo sin asistencia en <1 hora.

- [ ] **1.2 Soporte para formatos industriales**
  - Formato Excellon (coordenadas de agujeros de PCB) — es el estandar de facto.
  - Formato Gerber (complementario).
  - Parser de entrada + generador de secuencia de salida en formato compatible con controladores CNC.
  - Criterio de exito: leer un archivo Excellon real y devolver la secuencia optimizada.

- [ ] **1.3 Empaquetado y distribucion**
  - JAR para JVM, posiblemente biblioteca nativa para C/C++.
  - Licencia comercial definida (propietaria con licencia por maquina/instalacion).
  - Versionado semantico, changelog.
  - Criterio de exito: artefacto descargable y funcional.

- [ ] **1.4 Testing de integracion**
  - Tests con instancias reales de PCB drilling (obtener de clientes potenciales o de fuentes publicas).
  - Tests de estabilidad: miles de ejecuciones sin fallos.
  - Tests de rendimiento: medir tiempos en hardware tipico industrial (PC Windows, i5, 16GB RAM).
  - Criterio de exito: 0 fallos en 10,000 ejecuciones, tiempos dentro de requisitos.

### Fase 2: Validacion de mercado
**Objetivo:** Confirmar que hay demanda real y que el producto resuelve un problema que los clientes pagan por resolver.
**Estado:** [ ] No iniciada

- [ ] **2.1 Identificar contactos en empresas target**
  - Prioridad 1 (taladradoras PCB): Schmoll Maschinen, MKS/Excellon, Hitachi Via Mechanics, Posalux, LPKF.
  - Prioridad 2 (pick-and-place): Mycronic, Fuji, Yamaha.
  - Prioridad 3 (corte laser): TRUMPF, Bystronic.
  - Buscar: ingenieros de software, product managers, CTOs de division.
  - Criterio de exito: lista de 10+ contactos con nombre, empresa, cargo y email.

- [ ] **2.2 Preparar materiales de presentacion**
  - One-pager tecnico: que es, como funciona, benchmarks vs competidores.
  - Demo funcional: subir coordenadas -> ver tour optimizado + metricas.
  - Caso de negocio: "si taladras 10,000 PCBs/dia, nuestro solver te ahorra X horas de maquina/dia".
  - Criterio de exito: materiales revisados y listos para enviar.

- [ ] **2.3 Contactar y proponer piloto**
  - Email/LinkedIn a contactos identificados.
  - Proponer: "enviadnos un archivo Excellon real (anonimizado si es necesario), os devolvemos la secuencia optimizada con metricas comparativas gratis".
  - Objetivo: conseguir 1-3 pilotos gratuitos.
  - Criterio de exito: al menos 1 empresa acepta probar el solver con datos reales.

- [ ] **2.4 Ejecutar piloto**
  - Procesar instancias reales del cliente.
  - Comparar resultado con su solver actual (si comparten datos) o con LKH como referencia.
  - Documentar: calidad, tiempo, facilidad de integracion.
  - Criterio de exito: mejora demostrable en al menos un eje respecto a su solucion actual.

### Fase 3: Primeras ventas
**Objetivo:** Convertir pilotos en contratos de licencia.
**Estado:** [ ] No iniciada

- [ ] **3.1 Definir modelo de precios**
  - Opciones: licencia anual por maquina, por instalacion (site license), o por volumen de uso.
  - Referencia: herramientas EDA cobran $30K-$200K/ano por puesto. Solvers de optimizacion (Gurobi) cobran $6K-$60K/ano.
  - Punto de partida sugerido: $10K-$50K/ano por instalacion, dependiendo del tamano del cliente.
  - Criterio de exito: modelo de precios validado con al menos 1 cliente potencial.

- [ ] **3.2 Cerrar primer contrato**
  - Negociar terminos con cliente del piloto.
  - Contrato de licencia + soporte tecnico.
  - Criterio de exito: primer ingreso recibido.

- [ ] **3.3 Expandir a segunda vertical**
  - Con la credibilidad del primer cliente, contactar fabricantes de pick-and-place o corte laser.
  - Adaptar el solver si es necesario (restricciones adicionales del dominio).
  - Criterio de exito: segundo cliente en vertical diferente.

### Fase 4: Escalado (opcional, largo plazo)
**Objetivo:** Crecer mas alla del nicho inicial.
**Estado:** [ ] No iniciada

- [ ] **4.1 Publicacion academica** — si el algoritmo tiene propiedades teoricas novedosas, publicar en SODA/FOCS/STOC para credibilidad.
- [ ] **4.2 Patente** — patentar la implementacion especifica (los algoritmos puros no son patentables, pero implementaciones aplicadas si).
- [ ] **4.3 Reimplementacion en C/C++** — para integracion directa en firmware de maquinas (sin JVM).
- [ ] **4.4 Explorar adquisicion** — con clientes pagando y publicacion, se abre la posibilidad de ser adquirido por Synopsys, Cadence, Siemens, o similar.

---

## Dependencias criticas

| Dependencia | Fase que bloquea | Notas |
|---|---|---|
| Escalar a n=10,000+ | Fase 0 -> todo | Sin esto no hay producto viable para PCB drilling industrial |
| Benchmarking vs LKH | Fase 0 -> Fase 2 | Sin datos comparativos no hay argumento de venta |
| Formato Excellon | Fase 1 -> Fase 2 | Sin soporte de formato no hay piloto posible |
| Contactos en empresas | Fase 2 | Networking, LinkedIn, ferias industriales (Productronica, NEPCON) |
| Instancias reales de PCB | Fase 2 | Necesarias para validar a escala real |

---

## Riesgos y mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigacion |
|---|---|---|---|
| O(n^3) demasiado lento para n>10,000 | Alta | Alto | Particionamiento por broca reduce n. Investigar sub-cubico. |
| Fabricantes ya tienen solvers suficientemente buenos | Media | Alto | Enfocarse en nicho: empresas que NO tienen solver sofisticado (fabricantes pequenos, fabs en paises emergentes). |
| Google OR-Tools (gratuito) erosiona willingness to pay | Media | Medio | Diferenciarse en especializacion euclidea + garantia polinomica + soporte dedicado. |
| No se consiguen instancias reales de PCB | Media | Medio | Generar sinteticas basadas en patrones reales. Usar instancias TSPLIB de PCB. |
| Unico fundador tecnico, sin equipo comercial | Alta | Medio | Empezar con ventas directas (1-3 clientes). Buscar co-founder comercial si hay traccion. |

---

## Metricas de seguimiento

| Metrica | Objetivo Fase 0-1 | Objetivo Fase 2 | Objetivo Fase 3 |
|---|---|---|---|
| Instancias resueltas n>5,000 | 10+ | 50+ | 100+ |
| Gap vs optimo en n>5,000 | <2% | <2% | <2% |
| Tiempo de ejecucion n=10,000 | <60s | <60s | <30s |
| Empresas contactadas | 0 | 10+ | 20+ |
| Pilotos ejecutados | 0 | 1-3 | 3-5 |
| Contratos cerrados | 0 | 0 | 1+ |
| Ingresos anuales | $0 | $0 | $10K+ |

---

## Siguiente accion concreta

**Fase 0.1:** Importar instancias TSPLIB de n>5,000 (rl5915, rl5934, pla7397, rl11849, usa13509) y ejecutar el mejor solver actual para medir calidad y tiempo a esa escala.
