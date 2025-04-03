/* Initial beliefs and rules */

connect(kitchen, hall, doorKit1).
connect(kitchen, hallway, doorKit2).
connect(hall, kitchen, doorKit1).
connect(hallway, kitchen, doorKit2).
connect(bath1, hallway, doorBath1).
connect(bath2, bedroom1, doorBath2).
connect(hallway, bath1, doorBath1).
connect(bedroom1, bath2, doorBath2).
connect(bedroom1, hallway, doorBed1).
connect(hallway, bedroom1, doorBed1).
connect(bedroom2, hallway, doorBed2).
connect(hallway, bedroom2, doorBed2).
connect(bedroom3, hallway, doorBed3).
connect(hallway, bedroom3, doorBed3).
connect(hall, livingroom, doorSal1).        
connect(livingroom, hall, doorSal1).
connect(hallway, livingroom, doorSal2).       
connect(livingroom, hallway, doorSal2).     

// initially, robot is free
free.

// initially, I believe that there is some drug in the medcabinet
available(drug, medcabinet).

// my owner should not consume more than 10 drugs a day :-)
limit(drug,10).  
                 
too_much(B, Ag) :-
   .date(YY, MM, DD) &
   .count(consumed(YY, MM, DD, _, _, _, B, Ag), QtdB) &   
   limit(B, Limit) &    
   .println(Ag," has consumed ", QtdB, " ", B, " their limit is: ", Limit) &
   QtdB > Limit-1. 
   
answer(Request, "It will be nice to check the weather forecast, don't?.") :-
	.substring("tiempo", Request).  
	
answer(Request, "I don't understand what are you talking about.").

bringDrug(Ag) :- available(drug, medcabinet) & not too_much(drug, Ag).

orderDrug(Ag) :- not available(drug, medcabinet) & not too_much(drug, Ag).  

//Cantidad inicial de medicamentos,asumiremos que en esta simulación 20 es stock suficiente.
cantidad(paracetamol,20).
cantidad(ibuprofeno,20).
cantidad(aspirina,20).
cantidad(lorazepam,20).
cantidad(amoxicilina,20).

// Actualización de disponibilidad cuando cambia el inventario
+newAvailability(M,Qtd)<--cantidad(M,_);+cantidad(M,Qtd);.abolish(newAvailability(M,Qtd)).

/* Plans */
/*
+!has(Ag, drug)[source(Ag)] : 
	bringDrug(Ag) & free[source(self)] <- 
		.println("FIRST RULE ====================================");
		.wait(1000);
		//!at(enfermera, owner); 
    	-free[source(self)];      
		!at(enfermera, medcabinet);
		
		open(medcabinet); // Change it by an internal operation similar to fridge.open
		get(drug);    // Change it by a set of internal operations that recognize the drug an take it
		              // maybe it need to take other products and change their place in the fridge
		close(medcabinet);// Change it by an internal operation similar to fridge.close
		!at(enfermera, Ag);
		hand_in(drug);// In this case this operation could be external or internal their intention
		              // is to inform that the owner has the drug in his hand and could begin to drink
		?has(Ag, drug);  // If the previous action is completed then a perception from environment must update
		                 // the beliefs of the robot
						 
		// remember that another drug has been consumed
		.date(YY, MM, DD); .time(HH, NN, SS);
		+consumed(YY, MM, DD, HH, NN, SS, drug, Ag);
		+free[source(self)].  
*/
+!has(Ag, Med)[source(Ag)] : 
	bringDrug(Ag) & free[source(self)] <- 
		.println("FIRST RULE ====================================");
		.wait(1000);
		//!at(enfermera, owner); 
    	-free[source(self)];      
		!at(enfermera, medcabinet);
		
		open(medcabinet); // Change it by an internal operation similar to fridge.open
		get(Med);    // Change it by a set of internal operations that recognize the drug an take it
		              // maybe it need to take other products and change their place in the fridge
		close(medcabinet);// Change it by an internal operation similar to fridge.close
		!at(enfermera, Ag);
		hand_in(Med);// In this case this operation could be external or internal their intention
		              // is to inform that the owner has the drug in his hand and could begin to drink
		?has(Ag, Med);  // If the previous action is completed then a perception from environment must update
		                 // the beliefs of the robot
						 
		// remember that another drug has been consumed
		.date(YY, MM, DD); .time(HH, NN, SS);
		+consumed(YY, MM, DD, HH, NN, SS, drug, Ag);
		+free[source(self)].  
// This rule was changed in order to find the deliver in a different location 
// The door could be a good place to get the order and then go to the fridge
// and when the drug is there update the beliefs

+!has(Ag, drug)[source(Ag)] :
   	orderDrug(Ag) & free[source(self)] <- 
		.println("SECOND RULE ====================================");
		.wait(1000);
   		-free[source(self)]; 
		!at(enfermera, medcabinet);
		.send(repartidor, achieve, order(drug, 5)); 
		!at(enfermera, delivery);     // go to deliver area and wait there.
		.wait(delivered);
		!at(enfermera, medcabinet);       // go to medcabinet 
		deliver(Product,5);
		+available(drug, medcabinet); 
		+free[source(self)];
		.println("Trying to bring drug after order it");
		!has(Ag, drug)[source(Ag)].               

+!has(Ag, Med)[source(Ag)] :
   	orderDrug(Ag) & free[source(self)] <- 
		.println("SECOND RULE ====================================");
		.wait(1000);
   		-free[source(self)]; 
		!at(enfermera, medcabinet);
		.send(repartidor, achieve, order(Med, 5)); 
		!at(enfermera, delivery);     // go to deliver area and wait there.
		.wait(delivered);
		!at(enfermera, medcabinet);       // go to medcabinet 
		deliver(Med,5);
		+available(Med, medcabinet); 
		+free[source(self)];
		.println("Trying to bring drug after order it");
		!has(Ag, Med)[source(Ag)].  
// A different rule provided to not block the agent with contradictory petitions

+!has(Ag, drug)[source(Ag)] :
   	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, drug).   
		
+!has(Ag, drug)[source(Ag)] 
   :  too_much(drug, Ag) & limit(drug, L) <-
      	.println("FOURTH RULE ====================================");
		.wait(1000);
		.concat("The Department of Health does not allow me to give you more than ", L,
                " drugs a day! I am very sorry about that!", M);
		.send(Ag, tell, msg(M)).

+!has(Ag, Med)[source(Ag)] :
   	not free[source(self)] <- 
		.println("THIRD RULE ====================================");
		.println("The robot is busy and cann't attend the order now."); 
		.wait(4000);
		!has(Ag, Med).   
		
+!has(Ag, Med)[source(Ag)] 
   :  too_much(Med, Ag) & limit(Med, L) <-
      	.println("FOURTH RULE ====================================");
		.wait(1000);
		.concat("The Department of Health does not allow me to give you more than ", L,
                " medication a day! I am very sorry about that!", M);
		.send(Ag, tell, msg(M)).
// If some problem appears, we manage it by informing the intention that fails 
// and the goal is trying to satisfy. Of course we can provide or manage the fail
// better by using error annotations. Remember examples on slides when introducing
// intentions as a kind of exception      

-!has(Name, P) <-
//   :  true
// No condition is the same that a constant true condition
	.println("FIFTH RULE ====================================");
	.wait(1000);
	.current_intention(I);
    .println("Failed to achieve goal: !has(", Name, " , ", P, ").");
	.println("Current intention is: ", I).

+!at(Ag, P) : at(Ag, P) <- 
	.println(Ag, " is at ",P);
	.wait(500).
+!at(Ag, P) : not at(Ag, P) <- 
	.println("Going to ", P, " <=======================");  
	.wait(200);
	!go(P);                                        
	.println("Checking if is at ", P, " ============>");
	!at(Ag, P).            
	                                                   
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <- 
	.println("<================== 1 =====================>");
	.println("Al estar en la misma habitación se debe mover directamente a: ", P);
	move_towards(P).  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  connect(RoomAg, RoomP, Door) & not atDoor <-
	.println("<================== 3 =====================>");
	.println("Al estar en una habitación contigua se mueve hacia la puerta: ", Door);
	move_towards(Door); 
	!go(P).                     
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  connect(RoomAg, RoomP, Door) <- //& not atDoor <-
	.println("<================== 3 =====================>");
	.println("Al estar en la puerta de la habitación contigua se mueve hacia ", P);
	move_towards(P); 
	!go(P).       
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & not atDoor <-
	.println("<================== 4 =====================>");
	.println("Se mueve a: ", DoorR, " para ir a la habitación contigua, ", Room);
	move_towards(DoorR); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & atDoor <-
	.println("<================== 4 BIS =====================>");
	.println("Se mueve a: ", DoorP, " para acceder a la habitación ", RoomP);
	move_towards(DoorP); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <- //& not atDoor <-
	.println("Owner is at ", RoomAg,", that is not a contiguous room to ", RoomP);
	.println("<================== 5 =====================>");
	move_towards(P).                                                          
-!go(P) <- 
	.println("¿¿¿¿¿¿¿¿¿¿¿¿¿¿¿ WHAT A FUCK !!!!!!!!!!!!!!!!!!!!");
	.println("..........SOMETHING GOES WRONG......").                                        
	                                                                        
// when the supermarket makes a delivery, try the 'has' goal again
+delivered(drug, _Qtd, _OrderId)[source(repartidor)]
  :  true
  <- +delivered;
	 .wait(2000). 
	 
	 // Code changed from original example 
	 // +available(drug, fridge);
     // !has(owner, drug).

// When the fridge is opened, the drug stock is perceived
// and thus the available belief is updated
+stock(Med, 0)
   :  available(Med, medcabinet)
   <- -available(Med, medcabinet). 
   
+stock(Med, N)
   :  N > 0 & not available(Med, medcabinet)
   <- +available(Med, medcabinet).     
   
+chat(Msg)[source(Ag)] : answer(Msg, Answ) <-  
	.println("El agente ", Ag, " me ha chateado: ", Msg);
	.send(Ag, tell, msg(Answ)). 
                                     
+?time(T) : true
  <-  time.check(T).

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
            takeDrug(robot,M);
            !reducirCantidad(M);
            .print("He cogido ", M);
        };
        close(cabinet);
        !go_at(robot,owner);
        .send(owner,tell,espera);
        for(.member(pauta(M,H,F),L)) {
            .print("Le he dado ", M);
            handDrug(M);
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

// Planes para posicionarse junto al gabinete de medicamentos

// Plan para posicionarse al lado del gabinete de medicamentos
+!position_beside_cabinet : next_to(enfermera,medcabinet) <-
    .println("Enfermera ya está al lado del gabinete de medicamentos.");
    .wait(500).
+!position_beside_cabinet : not next_to(enfermera,medcabinet) <-
    .println("Enfermera se posiciona al lado del gabinete de medicamentos");
    position_next_to(medcabinet);
    !position_beside_cabinet.

// Plan para posicionarse al lado del propietario
+!position_beside_owner : next_to(enfermera,owner) <-
    .println("Enfermera ya está al lado del propietario.");
    .wait(500).
+!position_beside_owner : not next_to(enfermera,owner) <-
    .println("Enfermera se posiciona al lado del propietario");
    position_next_to(owner);
    !position_beside_owner.

// Plan para ir directamente al gabinete de medicamentos
+!go_to_medcabinet : at(robot, medcabinet) <-
    .println("Ya estoy en el gabinete de medicamentos.");
    .wait(500).
+!go_to_medcabinet : not at(robot, medcabinet) <-
    .println("Voy directamente al gabinete de medicamentos");
    !at(robot, medcabinet);
    .println("He llegado al gabinete de medicamentos").

// Plan para obtener medicación del gabinete
+!get_medication(Med) <-
    .println("Voy a obtener la medicación ", Med, " del gabinete");
    !go_to_medcabinet;
    open(medcabinet);
    take_medication(Med);
    close(medcabinet);
    .println("He obtenido la medicación ", Med).

// Plan para entregar medicamentos directamente al propietario
+!deliver_medication(Med)[source(Ag)] <-
    .println("Enfermera va a entregar medicación ", Med, " a ", Ag);
    !get_medication(Med);
    !position_beside_owner;
    .println("Enfermera entrega medicación ", Med, " a ", Ag);
    .send(Ag, tell, medication_delivered(Med)).
