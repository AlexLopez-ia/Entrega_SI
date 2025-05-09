// Este robot está diseñado para gestionar la entrega de medicamentos al propietario.

// El robot tiene un inventario de medicamentos y su disponibilidad inicial es 20 unidades para cada uno.
cantidad(paracetamol,20).
cantidad(ibuprofeno,20).
cantidad(aspirina,20).
cantidad(lorazepam,20).
cantidad(fent,20).

// Actualización de disponibilidad cuando cambia el inventario
+newAvailability(M,Qtd)<--cantidad(M,_);+cantidad(M,Qtd);.abolish(newAvailability(M,Qtd)).

!simularComportamiento.   
/* Plans */
//La simulación del robot es bastante sencilla,se desplaza a 3 posiciones.
+!simularComportamiento[source(self)]
   <- .random(X);
   .wait(3000*X + 5000);
          // wait for a random time
         .random([fridge,washer],Y);
         if(not .intend(go_at(robot,Y))){
	         .print("Voy a un sitio ",Y); 
             !go_at(robot,Y);    
      }
      !simularComportamiento.
// Robot comprueba en cada hora si tiene que tomar una medicina,en dicho caso entregará la lista de medicinas requerida.
+hour(H)<-
	.findall(pauta(M,H,F),.belief(pauta(M,H,F)),L);
     if(not L == []){
      if(not .intend(entregarMedicina(L))){
         .print("Hora de la medicina");
         !entregarMedicina(L);
      }
     }.

// Robot lanza un plan prioritario que es llevarle las medicinas al owner.
+!entregarMedicina(L)[source(self)]<-
    .print("Voy a entregar las medicinas");
    !bring(owner,L).

// Sistema de navegación simplificado
+!go_at(robot,P)[source(self)] : at(robot,P) <- 
    .print("He llegado a ",P).

+!go_at(robot,P)[source(self)] : not at(robot,P) <- 
    move_towards(P);
    !go_at(robot,P).

-!go_at(robot,P)[source(self)] <- 
    .print("Hay un obstáculo, intentando nuevamente");
    .wait(500);
    !go_at(robot,P).

// Manejo cuando el propietario solicita apartarse
+aparta[source(owner)] <- 
    .print("Debo apartarme");
    -aparta.

// Actualización de pautas después de la toma de medicamentos
+!resetearPauta(M)[source(self)] : pauta(M,H,F) <- 
    .abolish(pauta(M,H,F));
    if(H+F >= 24){
        Y = H+F-24;
    } else {
        Y = H+F;
    }
    +pauta(M,Y,F);
    .send(owner,tell,pauta(M,Y,F));
    .print("Próxima dosis de ", M, " a las ", Y, " horas").

// Plan para reducir las cantidades de medicamentos
+!reducirCantidad(M)[source(self)] : cantidad(M,H) <- 
    .abolish(cantidad(M,H)); 
    if(H-1 == 0){
        +cantidad(M,20);
        .print("Reiniciado a 20 el medicamento: ", M);
    }else{
    +cantidad(M,H-1);
    .print("Quedan ", H-1, " unidades de ", M)
    }.

// Manejo de la entrega de medicamento del auxiliar
+medicamentoEntregado(M)[source(auxiliar)] <- 
    .print("Medicamento ", M, " recibido del auxiliar");
    +medicamentoRecibido(M).

// Plan para solicitar un medicamento al auxiliar
+!solicitarMedicamento(M)[source(self)] <-
    .print("Solicitando medicamento ", M, " al auxiliar");
    .abolish(medicamentoRecibido(M));
    .send(auxiliar, tell, recogerMedicina(M));
    .print("Esperando a que el auxiliar recoja ", M, " y me lo traiga").

// Plan para esperar la entrega de un medicamento
+!esperarMedicamento(M)[source(self)] <-
    .print("Esperando entrega del medicamento ", M);
    .wait({+medicamentoRecibido(M)});
    .print("¡Medicamento ", M, " recibido correctamente del auxiliar!").

// Plan para entregar un medicamento específico al owner
+!entregarMedicamentoAlOwner(M)[source(self)] <-
    .print("Le he dado ", M);
    handInMedicamento(M);
    !resetearPauta(M).

// Plan para manejar fallos en la entrega de un medicamento específico
-!entregarMedicamentoAlOwner(M)[source(self)] <-
    .print("Error al entregar ", M, ", pero continuamos");
    !resetearPauta(M).

// Plan atómico para entregar medicamentos
+!bring(owner,L)[source(self)] <-
    if(not at(owner,cabinet) & not .belief(comprobarConsumo(_))) {
        .drop_intention(simularComportamiento);
        .send(owner, tell, quieto);
        
        for(.member(pauta(M,H,F),L)) {
            // Proceso de obtención del medicamento a través del auxiliar
            !solicitarMedicamento(M);
            !esperarMedicamento(M);
            
            // Si llegamos aquí, tenemos el medicamento
            !reducirCantidad(M);
            
            // Entrega del medicamento al propietario
            .send(owner, tell, espera);
            !go_at(robot, owner);
            
            // Usar un subplan para manejar la entrega (que puede fallar)
            !entregarMedicamentoAlOwner(M);
        }
        
        .send(owner, untell, quieto);
    } else {
        .wait(2000);
        .drop_intention(simularComportamiento);
        .findall(M,.belief(comprobarConsumo(M)),X);
        for(.member(M,X)) {
            .print("Compruebo el consumo de ",M);
            !comprobarConsumo(M);
            .abolish(comprobarConsumo(M));
        }
    }
    !simularComportamiento.

// Manejo de fallos en la entrega de medicamentos
-!bring(owner,L)[source(self)] <- 
    .print("Error al entregar medicamentos");
    .send(owner, untell, quieto);
    .send(owner, tell, falloEntrega);
    !simularComportamiento.

+!comprobarConsumo(M)[source(owner)] : cantidad(M,H) <- 
    open(cabinet);
    !comprobar(M,H);
    close(cabinet).

// Verificación del consumo de medicamentos
+!comprobarConsumo(M)[source(self)] : cantidad(M,H) <- 
    open(cabinet);
    !comprobar(M,H);
    close(cabinet).

// Plan para comprobar si el propietario realmente tomó el medicamento
+!comprobar(M,H)[source(self)] <- 
    comprobarConsumo(M,H);
    .print("Es verdad que ha cogido ",M);
    !reducirCantidad(M);
    !resetearPauta(M).

// Manejo cuando el propietario no ha tomado el medicamento
-!comprobar(M,H)[source(self)] <- 
    .print("No ha cogido ",M,"!");
    close(cabinet).

// Plan para verificar y reponer medicamentos
@comprueba[atomic]
+!comprueba(L) <- 
    !go_at(robot,cabinet);
    .wait(200);
    .findall(M,.belief(comprobarConsumo(M)),X);
    for(.member(M,X)) {
        .print("Compruebo el consumo de ",M);
        !comprobarConsumo(M);
        .abolish(comprobarConsumo(M));
    }
    !reponer.

// Plan para reponer medicamentos cuando se agotan
+!reponer <- 
    .print("Verificando inventario de medicamentos");
    .findall(M,.belief(pauta(M,_,_)) & cantidad(M,0),L);
    if(not L == []) {
        .print("Necesitamos reponer estas medicinas: ", L);
        // Reposición manual (sin robot auxiliar)
        for(.member(M,L)) {
            +cantidad(M,20);
            .print("Reponiendo ", M, ": ahora hay 20 unidades");
        }
    }.

// Owner le indica al robot la nueva pauta de medicinas
+nuevaPauta(M,H,F)[source(owner)] <- 
    .abolish(pauta(M,_,_));
    +pauta(M,H,F);
    .abolish(nuevaPauta(M,H,F));
    .print("Actualizada pauta de ", M, " para las ", H, " horas").

@recibir[atomic]
+!recibir(L)<-
		 .send(owner,tell,espera);
         for(.member(pauta(M,H,F),L))
         {
		 	!reducirCantidad(M);
		 	.print("Le he dado ", M);
            handInMedicamento(M);
			!resetearPauta(M);
         }
		 !reponer.

// Plan para manejar notificaciones de medicamentos caducados
+esperaMedicamentoNuevo(M)[source(auxiliar)] <-
    .print("El auxiliar me informa que ", M, " está caducado");
    .print("Esperando a que el auxiliar reponga el medicamento ", M);
    .wait({+medicamentoEntregado(M)});
    .print("El auxiliar ha repuesto y entregado el medicamento ", M).

