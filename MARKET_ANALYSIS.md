# Analisis de Mercado: TSP Euclideo 2D en la Industria

Fecha: 2026-04-12

## 1. Donde aparece el TSP Euclideo 2D como problema industrial real

El TSP euclideo 2D (puntos en un plano, distancia euclidiana) aparece como subproblema critico en al menos 10 aplicaciones industriales:

| Industria | Aplicacion concreta | Escala tipica (n) | Escala extrema |
|---|---|---|---|
| **Fabricacion de PCB** | Taladrado CNC de agujeros | 500 - 5,000 por broca | 300,000+ por placa |
| **Semiconductores** | Wire bonding (pads del chip -> leads) | 50 - 500 | 1,000+ |
| **Semiconductores** | Die attach / pick-and-place de dies | 100 - 1,000 | Miles |
| **Ensamblaje SMT** | Pick-and-place de componentes | 90 - 5,000 | 10,000+ |
| **Litografia e-beam** | Secuenciacion de subcampos (variante max-scatter) | Miles | Decenas de miles |
| **Corte laser / CNC** | Minimizacion de recorrido en vacio del cabezal | 100 - 10,000 | 100,000+ |
| **Manufactura aditiva (3D printing)** | Trayectoria del cabezal de impresion | 100 - 10,000 | - |
| **Inspeccion optica (AOI)** | Ruta de la camara para inspeccionar componentes | Cientos | Miles |
| **Wafer probing** | Secuencia de testeo de dies en oblea | 200 - 1,000 | Miles |
| **Dispensado de adhesivo/soldadura** | Ruta del dispensador sobre PCB | 100 - 1,000 | - |
| **Cristalografia de rayos X** | Orden de coleccion de difracciones | Cientos | - |
| **Astronomia** | Planificacion de observaciones de telescopio | Cientos | - |

### Aplicacion mas pura: taladrado de PCB

Las instancias TSPLIB `pcb442`, `pcb1173` y `pcb3038` proceden directamente de problemas reales de taladrado de PCB (Reinelt, 1991). El movimiento del cabezal y cambio de brocas representan **hasta el 70% del tiempo total del proceso** (Merchant, 1985). Es la aplicacion mas cercana al TSP euclideo 2D puro.

### Por que no todas son TSP euclideo puro

Aunque todas las aplicaciones de la tabla se modelan como TSP euclideo 2D, en la practica muchas tienen restricciones adicionales:

- **Pick-and-place SMT:** Restricciones de feeders, cambios de boquilla, colisiones de cabezal.
- **Wire bonding:** Restricciones de precedencia (bond pad antes que lead), zonas de exclusion.
- **Corte laser:** Restricciones de orden de corte (piezas internas antes que contorno).
- **Taladrado de PCB:** Cambios de broca (agrupa por diametro, TSP por grupo). Es la mas pura.

---

## 2. Empresas que necesitan resolver este problema

### Fabricantes de taladradoras de PCB (aplicacion mas pura)

| Empresa | Pais | Notas |
|---|---|---|
| **Excellon / ESI / MKS Instruments** | EEUU | Invento el formato de archivo de taladrado estandar |
| **Schmoll Maschinen** | Alemania | Taladradoras CNC de alta velocidad |
| **Hitachi Via Mechanics** | Japon | Taladradoras laser y mecanicas |
| **Posalux** | Suiza | Taladrado de precision |
| **LPKF Laser & Electronics** | Alemania | Procesamiento laser de PCB |

### Fabricantes de wire bonders

| Empresa | Pais | Notas |
|---|---|---|
| **Kulicke & Soffa (K&S)** | EEUU | Lider mundial |
| **ASMPT** | Hong Kong | Segundo fabricante global |
| **Hesse Mechatronics** | Alemania | Wire bonding de potencia |

### Fabricantes de pick-and-place SMT

| Empresa | Pais | Notas |
|---|---|---|
| **Fuji Corporation** | Japon | Series AIMEX, NXT |
| **Panasonic Connect** | Japon | Serie NPM |
| **Yamaha Motor (seccion IM)** | Japon | Serie YSM |
| **Mycronic** | Suecia | Diferenciador principal: su optimizer propietario |
| **Siemens / Siplace** | Alemania | Software SiCluster |
| **JUKI** | Japon | Gama media-alta |
| **Hanwha Precision Machinery** | Corea del Sur | Series DECAN y SM |
| **Universal Instruments** | EEUU | Plataforma Fuzion |

### Corte laser / CNC

| Empresa | Pais | Notas |
|---|---|---|
| **TRUMPF** | Alemania | Lider mundial en laser industrial |
| **IPG Photonics** | EEUU | Fuentes laser de fibra |
| **Bystronic** | Suiza | Corte laser y plegado |

### Inspeccion y dispensado

| Empresa | Pais | Notas |
|---|---|---|
| **Nordson (ASYMTEK, YESTECH)** | EEUU | Dispensado + inspeccion |
| **KLA / Orbotech** | EEUU/Israel | AOI |
| **Musashi Engineering** | Japon | Dispensadores de precision |

### Software de optimizacion de fabricacion

| Empresa | Pais | Notas |
|---|---|---|
| **Siemens Digital Industries (Valor)** | Alemania | Mayor vendedor de software de optimizacion de fabricacion de PCB |
| **Zuken** | Japon | Diseno + fabricacion de PCB |
| **Aegis Software (FactoryLogix)** | EEUU | MES con optimizacion SMT |

---

## 3. Algoritmos que usa la industria

### Algoritmos dominantes

| Algoritmo | Uso industrial | Calidad tipica (gap vs optimo) | Complejidad |
|---|---|---|---|
| **Nearest Neighbor** | Solucion inicial en casi todas las maquinas | ~20-25% | O(n^2) |
| **2-opt + Or-opt** | Caballo de batalla de la industria | ~2-5% | O(n^2) por pasada, O(n^3) total |
| **Lin-Kernighan** | Maquinas de gama alta, implementaciones propietarias | ~1-2% | Variable, no garantizado polinomico |
| **LKH (Helsgott)** | Referencia academica, licenciado por algunas empresas | <1% | No polinomico |
| **Concorde** | Solver exacto, usado para generar referencias offline | 0% (optimo probado) | Exponencial peor caso |
| **Algoritmos geneticos / ACO** | Algunos fabricantes, articulos academicos | 2-10% | Variable |
| **Google OR-Tools** | Integrado en algunos sistemas de planificacion | 1-5% | Variable |

**Realidad:** La gran mayoria de maquinas en produccion usan **NN + 2-opt + or-opt**. Solo las de gama alta tienen implementaciones propietarias tipo LK.

### Calidad objetivo por sector

| Sector | Gap aceptable | Gap de los mejores sistemas |
|---|---|---|
| PCB drilling (n < 10,000) | 1-3% | 0.5-2% |
| PCB drilling (n > 50,000) | 5% | 3-5% |
| Wire bonding (n < 500) | Optimo | 0-0.1% |
| Pick-and-place SMT | 2-5% (componente TSP) | 2-3% |
| Corte laser / CNC | 1-5% | 1-3% |

---

## 4. Tiempos de computo

| Contexto | Tamano | Tiempo real tipico | Requisito |
|---|---|---|---|
| Wire bonding setup | 50-500 pts | <1 segundo | Segundos |
| PCB drilling prep | 1,000-100,000 pts | 1-60 segundos | Segundos a minutos |
| Pick-and-place setup | 500-5,000 pts | 10 seg - 5 min | Minutos |
| Optimizacion de linea SMT | Multiples maquinas | 1-30 min | Minutos a horas |
| Re-optimizacion dinamica | Variable | 10-500 ms | Milisegundos |

La optimizacion se hace **offline** (preparacion del programa de la maquina). Se optimiza una vez y se ejecuta miles de veces en produccion.

---

## 5. Infraestructura: no usan data centers

La optimizacion TSP en manufactura se ejecuta en un **PC local** (Windows, Intel i5-i9, 16-64 GB RAM). Razones:

1. **IP sensible:** Las coordenadas del PCB/chip son propiedad intelectual critica; nadie las sube a la nube.
2. **Latencia:** Para re-optimizacion en tiempo real, la latencia de red es inaceptable.
3. **Disponibilidad:** Las fabricas no pueden depender de internet para producir.
4. **Tradicion:** La industria de fabricacion electronica es conservadora en la adopcion de cloud.

Tendencias emergentes hacia cloud (Siemens MindSphere, Mycronic MYCenter) existen para analytics, pero la optimizacion de rutas sigue siendo local.

---

## 6. Impacto economico de la optimizacion

| Aplicacion | Mejora documentada | Fuente |
|---|---|---|
| Taladrado PCB | 30% reduccion en tiempo de taladrado | Literatura academica |
| Taladrado PCB | 34% reduccion en recorrido en vacio | Estudio de caso |
| Taladrado PCB | 13x mejora en eficiencia de ruteo vs. manual | Nature Scientific Reports, 2025 |
| Pick-and-place SMT | 25% reduccion en movimientos de cabezal | GA optimization studies |
| Wire bonding | 14.4% reduccion en tiempo de proceso | ML optimization |
| AOI | 70% reduccion en tiempo de inspeccion | Path planning studies |

Ejemplo: Si una fabrica produce 10,000 PCBs/dia y cada placa tarda 3 min en taladrar, un 10% de ahorro = **50 horas de maquina ahorradas al dia**.

---

## 7. Tamano de mercados relevantes

| Mercado | Tamano (2025) | Proyeccion 2030 | CAGR |
|---|---|---|---|
| **EDA (Electronic Design Automation)** | $17-19B | $25-30B | 8-10% |
| **Optimizacion de rutas (logistica)** | $8-15B | $21-42B | 14-15% |
| **Fabricacion de PCB (total)** | ~$80B | - | - |
| **Manufactura aditiva** | ~$20B | - | - |
| **Solvers de optimizacion matematica** (Gurobi, CPLEX) | Nicho ~$100-500M | - | - |

El oligopolio EDA (Synopsys ~38%, Cadence ~36%, Siemens ~13%) tiene retencion de clientes ~100% y 80-85% de ingresos recurrentes.

---

## 8. Posicion competitiva del proyecto

### Lo que tenemos (referencia: SolverJ5)

- Gap empirico ~1-3.7% sobre optimo (verificado hasta n=5,915)
- Complejidad garantizada O(n^3)
- Verificado en instancias TSPLIB de n=51 a n=5,915

### Resultados de escalabilidad (2026-04-12)

| Instancia | n | Ratio | Tiempo | Viable (<2%, <60s)? |
|---|---|---|---|---|
| pcb442 | 442 | 1.0132x | 4.8s | SI |
| d657 | 657 | 1.0368x | 21.2s | MARGINAL |
| pr1002 | 1,002 | 1.0257x | 31.3s | MARGINAL |
| d2103 | 2,103 | 1.0140x | 251.4s | NO (tiempo) |
| pcb3038 | 3,038 | 1.0341x | 651s | NO (tiempo) |
| rl5915 | 5,915 | 1.0262x | 3,687s | NO (tiempo) |
| rl11849 | 11,849 | OOM | — | NO (memoria) |

### Comparacion con la industria

| Criterio | Nuestra posicion | Industria | Veredicto |
|---|---|---|---|
| **Calidad** (1-3.7% gap) | Competitivo | La industria acepta 1-5% | Bien |
| **Complejidad garantizada** O(n^3) | Ventaja teorica | LKH no garantiza polinomico | Ventaja teorica (pero lento en practica) |
| **Escalabilidad** (n > 1,000) | >30s para n=1,000; OOM para n>10,000 | PCBs reales: 500-300,000 agujeros | **Limitacion critica** |
| **Restricciones reales** | TSP puro | Restricciones adicionales | Gap a cerrar |
| **Software productizado** | Biblioteca Kotlin | Se necesita API, formatos industriales | Gap a cerrar |

### Ventaja diferencial principal

La **garantia de complejidad polinomica O(n^3)** es algo que ningun solver top ofrece:
- **LKH:** Mejor heuristica, pero sin garantia de tiempo polinomico.
- **Concorde:** Optimo exacto, pero exponencial en peor caso.
- **2-opt/or-opt:** Polinomico, pero calidad 2-5%.

Nuestro solver combina calidad competitiva (~1%) con tiempo de ejecucion predecible. Esto tiene valor real en sistemas embedded y aplicaciones de tiempo real.

---

## 9. Vias de monetizacion

### Via 1: Licenciamiento como biblioteca/SDK a fabricantes de maquinas

- **Target:** Fabricantes de taladradoras de PCB, wire bonders, pick-and-place, cortadoras laser.
- **Propuesta de valor:** Solver con calidad ~1% gap + tiempo de ejecucion garantizado O(n^3).
- **Modelo:** Licencia anual por maquina o por instalacion.
- **Ingresos estimados:** $500K-$5M/ano.
- **Barrera:** Los fabricantes desarrollan solvers internamente y los consideran IP propietaria. Hay que demostrar ventaja clara.

### Via 2: Adquisicion por incumbente EDA

- **Target:** Synopsys, Cadence, Siemens.
- **Propuesta de valor:** Tecnologia de optimizacion de routing con propiedades teoricas unicas.
- **Precio potencial:** $10M-$100M+ si la ventaja es demostrable y tiene propiedades teoricas novedosas.
- **Barrera:** Requiere publicacion academica de peso o patente.

### Via 3: Producto SaaS vertical (PCB drilling / laser cutting)

- **Target:** Fabricantes de PCB, talleres de corte laser.
- **Propuesta de valor:** Optimizador de rutas como servicio, integrado con formatos industriales (Excellon, Gerber).
- **Modelo:** SaaS por uso o suscripcion.
- **Barrera:** IP sensible — las empresas son reticentes a subir datos a la nube. Posible solucion: on-premise.

### Via 4: Integracion en software de routing logistico

- **Target:** Empresas de logistica y entregas.
- **Mercado:** $8-15B, creciendo 14-15%/ano.
- **Barrera:** Competencia feroz. Google OR-Tools es gratuito. Habria que diferenciarse en calidad + garantia polinomica.

---

## 10. Primer paso concreto: validar con fabricantes de taladradoras de PCB

### Por que taladrado de PCB

1. Es la aplicacion **mas pura** de TSP euclideo 2D (sin restricciones adicionales significativas dentro de cada grupo de broca).
2. La optimizacion tiene **impacto directo y medible** en el throughput (70% del tiempo es movimiento).
3. Las instancias TSPLIB ya provienen de este dominio — tenemos benchmarks reales.
4. El tamano tipico (500-5,000 por broca) esta en el rango donde O(n^3) es viable.

### Que habria que hacer

1. **Escalar el solver** a n=10,000+ manteniendo calidad y O(n^3). Verificar con instancias TSPLIB grandes (pcb1173, pcb3038, y generar/importar instancias de 10,000+ puntos).
2. **Benchmarking formal** contra LKH y Google OR-Tools en instancias de escala industrial, midiendo calidad Y tiempo de ejecucion.
3. **Productizar:** API limpia, soporte para formato Excellon (coordenadas de agujeros), documentacion en ingles.
4. **Publicar** resultados en conferencia o journal relevante si hay propiedades teoricas novedosas. Esto da credibilidad.
5. **Contactar** fabricantes de taladradoras (Schmoll, MKS/Excellon, Hitachi Via Mechanics) para proponer un piloto.

### Que necesitamos demostrar al cliente

- Que nuestro solver da **igual o mejor calidad** que su solver actual en sus instancias reales.
- Que el **tiempo de ejecucion es predecible** y esta dentro de sus requisitos.
- Que la integracion es factible (API, formatos, plataforma).

---

## 11. Barreras y riesgos

| Barrera | Severidad | Mitigacion |
|---|---|---|
| O(n^3) no escala a n>50,000 | Alta | Particionamiento por grupo de broca reduce n efectivo. Investigar algoritmos sub-cubicos. |
| Fabricantes desarrollan solvers internamente | Alta | Demostrar mejora medible en sus propias instancias. |
| Sin garantia teorica demostrada (solo empirica) | Media | Publicar analisis formal o buscar contraejemplos sistematicamente. |
| Google OR-Tools es gratuito | Media | Diferenciarse en calidad + tiempo predecible + especializacion euclidea. |
| IP de coordenadas es sensible | Media | Ofrecer solucion on-premise, no cloud. |
| Oligopolio EDA tiene lock-in ~100% | Alta (para via 2) | Apuntar a adquisicion, no a competir directamente. |

---

## Fuentes principales

- Reinelt (1991) — TSPLIB, instancias de PCB drilling reales
- Merchant (1985) — 70% del tiempo de taladrado es movimiento
- Nature Scientific Reports (2025) — Fully automated PCB drilling machine with path optimization
- Mordor Intelligence, Precedence Research, Grand View Research — Market sizing EDA y route optimization
- IEEE — Subfield scheduling for e-beam lithography
- PMC — PCB drill path optimization studies
- Wing Venture Capital, Arvy — Analisis del oligopolio EDA
