// Agente asistente para la gestión de medicamentos

// Plan principal de comportamiento
!simularComportamiento.

// Plan para manejar mensajes de recogida de medicinas del robot
+recogerMedicina(M)[source(robot)] <-
    .print("Recibida solicitud de recogida de medicamento ", M);
    .print("Estado: Iniciando recogida de ", M);
    // Ir al gabinete y coger la medicina
    .print("Voy al gabinete de medicamentos");
    !go_at(auxiliar, cabinet);
    .print("Abriendo gabinete");
    open(cabinet);
    .print("Cogiendo medicamento ", M, " del gabinete");
    takeMedication(auxiliar, M);
    .print("Medicamento ", M, " recogido del gabinete");
    +medicamentoRecogido(M);
    close(cabinet);
    .print("Gabinete cerrado, tengo el medicamento ", M);
    
    // Ir al robot y entregar la medicina
    .print("Voy a entregar la medicina al robot");
    !go_at(auxiliar, robot);
    .print("He llegado al robot para entregar la medicina");
    .print("Entregando medicamento ", M, " al robot");
    .abolish(medicamentoRecogido(M));
    .send(robot, tell, medicamentoEntregado(M));
    .print("Medicamento entregado con éxito al robot").

// Plan para manejar medicamentos caducados detectados por el auxiliar
+medicinaCaducada(M)[source(percept)] <-
    .print("¡He detectado que el medicamento ", M, " está caducado!");
    .send(robot, tell, esperaMedicamentoNuevo(M));
    .print("Notificando al robot que espere mientras voy a buscar ", M, " nuevo");
    // Simular ir al pickup a por nueva medicina
    !simularRecargaMedicina(M);
    // Continuar con el proceso original después de reponer
    .print("Ahora intentaré recoger el medicamento ", M, " recién repuesto");
    // Estamos ya en el gabinete, así que podemos coger directamente la medicina
    .print("Cogiendo medicamento ", M, " del gabinete");
    open(cabinet);
    takeMedication(auxiliar, M);
    .print("Medicamento ", M, " recogido del gabinete");
    +medicamentoRecogido(M);
    close(cabinet);
    .print("Gabinete cerrado, tengo el medicamento ", M);
    
    // Ir al robot y entregar la medicina
    .print("Voy a entregar la medicina al robot");
    !go_at(auxiliar, robot);
    .print("He llegado al robot para entregar la medicina");
    .print("Entregando medicamento ", M, " al robot");
    .abolish(medicamentoRecogido(M));
    .send(robot, tell, medicamentoEntregado(M));
    .print("Medicamento entregado con éxito al robot").

// Plan para manejar solicitudes del owner para reponer medicamentos caducados
+reponerMedicinaCaducada(M)[source(owner)] <-
    .print("Recibida solicitud del propietario para reponer ", M, " caducado");
    // Simular ir al pickup a por nueva medicina
    !simularRecargaMedicina(M).

// Plan para simular la recarga de una medicina
+!simularRecargaMedicina(M) <-
    .print("Voy al punto de recogida a buscar ", M, " nuevo");
    !go_at(auxiliar, pickup);
    .print("He llegado al punto de recogida");
    .print("Recogiendo nuevo suministro de ", M);
    .wait(2000); // Simular tiempo de recogida
    .print("Suministro de ", M, " recogido, volviendo al gabinete");
    !go_at(auxiliar, cabinet);
    .print("He llegado al gabinete con el nuevo suministro de ", M);
    .print("Guardando ", M, " en el gabinete");
    open(cabinet);
    .wait(1000); // Simular tiempo guardando
    close(cabinet);
    .print("Medicamento ", M, " repuesto en el gabinete");
    .send(owner, tell, medicamentoRepuesto(M));
    .abolish(medicinaCaducada(M)).

// Plan alternativo si falla la simulación de recarga
-!simularRecargaMedicina(M) <-
    .print("Hubo un problema simulando la recarga de ", M, ", reintentando");
    !simularRecargaMedicina(M).

// Sistema de navegación mejorado
+!go_at(auxiliar, P)[source(self)] : at(auxiliar, P) <- 
    .print("He llegado a ", P).

+!go_at(auxiliar, P)[source(self)] : not at(auxiliar, P) <- 
    move_towards(P);
    .wait(500); // Esperar feedback del entorno
    !go_at(auxiliar, P).

// Control de intentos para evitar bucles infinitos
-!go_at(auxiliar, P)[source(self)] <-
    .print("Problema al navegar hacia ", P, ", intentando de nuevo");
    .wait(1000);  // Espera más larga antes de reintentar
    !go_at(auxiliar, P).

// Plan para simular el comportamiento del auxiliar
+!simularComportamiento[source(self)] <-
    .print("Auxiliar listo para asistir");
    .wait(5000);  // Espera 5 segundos entre verificaciones
    !simularComportamiento.

// Plan para manejar la posición actual
+at(auxiliar, P)[source(percept)] <-
    .print("Auxiliar está en ", P).

// Plan para manejar la posición del propietario
+at(owner,P)[source(self)] <-
    .print("El propietario está en ", P).

// Plan para manejar la posición del gabinete
+at(cabinet,P)[source(self)] <-
    .print("El gabinete está en ", P).

// Plan para manejar la posición del robot
+at(robot, P)[source(percept)] <-
    .print("El robot está en ", P). 