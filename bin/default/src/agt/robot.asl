// Robot para la gestión de medicamentos

//Cantidad inicial de medicamentos,asumiremos que en esta simulación 20 es stock suficiente.
cantidad(paracetamol,20).
cantidad(ibuprofeno,20).
cantidad(aspirina,20).
cantidad(lorazepam,20).
cantidad(amoxicilina,20).

// Actualización de disponibilidad cuando cambia el inventario
+newAvailability(M,Qtd)<--cantidad(M,_);+cantidad(M,Qtd);.abolish(newAvailability(M,Qtd)).

!simulate_behaviour.   
/* Plans */
//La simulación del robot es bastante sencilla,se desplaza a 3 posiciones.
+!simulate_behaviour[source(self)]
   <- .random(X);
   .wait(3000*X + 5000);
          // wait for a random time
         .random([fridge,washer],Y);
         if(not .intend(go_at(robot,Y))){
	         .print("Voy a un sitio ",Y); 
             !go_at(robot,Y);    
      }
      !simulate_behaviour.
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
    +cantidad(M,H-1);
    .print("Quedan ", H-1, " unidades de ", M).

// Plan atómico para entregar medicamentos
@medicina[atomic]
+!bring(owner,L)[source(self)] <- 
    !go_at(robot,cabinet);
    if(not at(owner,cabinet) & not .belief(comprobarConsumo(_))){
        .send(owner, tell, quieto);
        open(cabinet);
        for(.member(pauta(M,H,F),L)) {
            takeMedication(robot,M);
            !reducirCantidad(M);
            .print("He cogido ", M);
        };
        close(cabinet);
        !go_at(robot,owner);
        .send(owner,tell,espera);
        for(.member(pauta(M,H,F),L)) {
            .print("Le he dado ", M);
            handInMedicamento(M);
            !resetearPauta(M);
        }
        .send(owner, untell, quieto);
    } else {
        .wait(2000);
        .findall(M,.belief(comprobarConsumo(M)),X);
        for(.member(M,X)) {
            .print("Compruebo el consumo de ",M);
            !comprobarConsumo(M);
            .abolish(comprobarConsumo(M));
        }
    };
    !reponer.

//Regla de error

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
+pautaNueva(M,H,F)[source(owner)] <- 
    .abolish(pauta(M,_,_));
    +pauta(M,H,F);
    .abolish(pautaNueva(M,H,F));
    .print("Actualizada pauta de ", M, " para las ", H, " horas").

@recibir[atomic]
+!recibir(L)<-
		 .send(owner,tell,espera);
         for(.member(pauta(M,H,F),L))
         {
		 	!reducirCantidad(M);
		 	.print("Le he dado ", M);
            handDrug(M);
			!resetearPauta(M);
         }
		 !reponer.