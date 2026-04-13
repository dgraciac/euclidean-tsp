package com.github.dgraciac.euclideantsp

// Instancias TSPLIB grandes (n > 2103) cargadas desde ficheros .tsp en resources.
// Se usan ficheros en lugar de coordenadas inlineadas porque el JVM tiene un
// limite de 64KB por metodo, y setOf() con >~8000 puntos excede ese limite.
// Los optimos son los valores oficiales de TSPLIB (distancias EUC_2D redondeadas a entero).

/** Instancia TSPLIB pcb3038: 3038 puntos — PCB drilling real (Junger/Reinelt). */
val PCB_3038 by lazy { parseTspLibResource("tsplib/pcb3038.tsp", 137694.0) }

/** Instancia TSPLIB rl5915: 5915 puntos (Reinelt). */
val RL_5915 by lazy { parseTspLibResource("tsplib/rl5915.tsp", 565530.0) }

/** Instancia TSPLIB rl11849: 11849 puntos (Reinelt). */
val RL_11849 by lazy { parseTspLibResource("tsplib/rl11849.tsp", 923288.0) }

/** Instancia TSPLIB usa13509: 13509 puntos — Ciudades de EEUU con poblacion >= 500. */
val USA_13509 by lazy { parseTspLibResource("tsplib/usa13509.tsp", 19982859.0) }
