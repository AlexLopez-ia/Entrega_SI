/* Initial Beliefs */
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
connect(hall,livingroom, doorSal1).                       
connect(livingroom, hall, doorSal1).
connect(hallway,livingroom, doorSal2).
connect(livingroom, hallway, doorSal2).

/* Initial goals */

// Owner will simulate the behaviour of a person 
// We need to characterize their digital twin (DT)
// Owner must record the DT data periodically 
// Owner must access the historic data of such person
// Owner will act randomly according to some problems
// Owner will usually act with a behaviour normal
// Owner problems will be activated by some external actions
// Owner problems will randomly be activated on time
// Owner will dialog with the nurse robot 
// Owner will move randomly in the house by selecting places

!sit.

!open.

!walk.

!wakeup.

!check_bored.

// Initially Owner could be: sit, opening the door, waking up, walking, ...
//!sit.   			
//!check_bored. 

//+!init <- !sit ||| !open ||| !walk ||| !wakeup ||| !check_bored.


!pauta_medicamentos.

//Pautas:nombre,hora,frecuencia.
pauta(paracetamol,8,6).
pauta(ibuprofeno,12,6).
pauta(lorazepam,22,23).
pauta(aspirina,8,8).
pauta(amoxicilina,15,2).


//El robot es quien controla los horarios,es decir,actualiza tras una consumici�n,por lo tanto debe indicarle al owner la nueva hora.
+pauta(M,H,F)[source(robot)] <- .abolish(pauta(M,H-F,_)).
+!pauta_medicamentos 
   <- .findall(pauta(A,B,C),.belief(pauta(A,B,C)),L);
   	  for(.member(I,L))
	  {
	  	.send(robot,tell,I);
	  }.

//Owner va por las medicinas,por tanto suspende su comportamiento.
//@tomarMedicina[atomic]
+!tomarMedicina(L)[source(self)]<-
   if(.intend(simulate_behaviour)){
      .drop_intention(simulate_behaviour);
   }
   !tomar(owner,L);
   !simulate_behaviour.
//Owner va hacia el cabinet,selecciona las medicinas y las toma.En su conclusi�n,informa al robot de la consumici�n.
+!tomar(owner,L)[source(self)]
   <- !go_at(owner,cabinet);
      if(not at(robot,cabinet) & not quieto){
         open(cabinet);
		 .send(robot,achieve,comprueba(L));
         for(.member(pauta(M,H,F),L))
         {
            .abolish(pauta(M,H,F));
      	   takeDrug(owner,M);
            .print("He cogido la medicina ", M);
            .send(robot, tell, comprobarConsumo(M));
         };
         for(.member(pauta(M,H,F),L))
         {
            handDrug(M);
         }
         close(cabinet);
      }.
//Cuando el robot le da las medicinas en la mano,le pide al owner que se quede quieto.
+espera :not durmiendo<- 
	if(.intend(simulate_behaviour))
	{
		.drop_intention(simulate_behaviour);
		!simulate_behaviour;
	};
	.abolish(espera).

+!wakeup : .my_name(Ag) & not busy <-
	+busy;
	!check_bored;
	.println("Owner just woke up and needs to go to the fridge"); 
	.wait(3000);
	-busy;
	!sit.
+!wakeup : .my_name(Ag) & busy <-
	.println("Owner is doing something now, is not asleep");
	.wait(10000);
	!wakeup.
	
+!walk : .my_name(Ag) & not busy <- 
	+busy;  
	.println("Owner is not busy, is sit down on the sofa");
	.wait(500);
	!at(Ag,sofa);
	.wait(2000);
	//.println("Owner is walking at home"); 
	-busy;
	!open.
+!walk : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not walk");
	.wait(6000);
	!walk.

+!open : .my_name(Ag) & not busy <-
	+busy;   
	.println("El propietario va a la puerta de casa");
	.wait(200);
	!at(Ag, delivery);
	.println("El propietario está abriendo la puerta"); 
	.random(X); .wait(X*7351+2000); // El propietario tarda un tiempo aleatorio en abrir la puerta 
	!at(Ag, sofa);
	sit(sofa);
	.wait(5000);
	!at(Ag, medcabinet);
	.wait(10000);
	!at(Ag, chair3);
	sit(chair3);
	-busy.
+!open : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not open the door");
	.wait(8000);
	!open.
 
+!sit : .my_name(Ag) & not busy <- 
	+busy; 
	.println("El propietario va al gabinete de medicamentos.");
	.wait(1000);
	!at(Ag, medcabinet);
	.println("El propietario está en el gabinete de medicamentos buscando sus medicinas"); 
	//.println("He llegado al frigorifico");
	.wait(2000);
	!at(Ag, chair3);
	sit(chair3);
	.wait(4000);
	!at(Ag, chair4);
	sit(chair4);
	.wait(4000);
	!at(Ag, chair2);
	sit(chair2);
	.wait(4000);
	!at(Ag, chair1);
	sit(chair1);
	.wait(4000);
	!at(Ag, sofa);
	sit(sofa);
	.wait(10000);
	!get(Med); 
	.wait(50000);
	-busy.
+!sit : .my_name(Ag) & busy <-
	.println("Owner is doing something now and could not go to fridge");
	.wait(30000);
	!sit.

+!at(Ag, P) : at(Ag, P) <- 
	.println("Owner is at ",P);
	.wait(5000).
+!at(Ag, P) : not at(Ag, P) <- 
	.println("Going to ", P);
	!go(P);                                        
	.println("Checking if is at ", P);
	!at(Ag, P).            
	                                                   
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomAg) <-                             
	.println("Al estar en la misma habitación se debe mover directamente a: ", P);
	move_towards(P).  
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  connect(RoomAg, RoomP, Door) & atDoor <-
	.println("Al estar en la puerta ", Door, " se dirige a ", P);                        
	move_towards(P); 
	!go(P).       
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  connect(RoomAg, RoomP, Door) & not atDoor <-
	.println("Al estar en una habitación contigua se mueve hacia la puerta: ", Door);
	move_towards(Door); 
	!go(P).       
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & not atDoor <-
	.println("Se mueve a: ", DoorR, " para ir a la habitación contigua, ", Room);
	move_towards(DoorR); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP &
		  not connect(RoomAg, RoomP, _) & connect(RoomAg, Room, DoorR) &
		  connect(Room, RoomP, DoorP) & atDoor <-
	.println("Se mueve a: ", DoorP, " para ir a la habitación ", RoomP);
	move_towards(DoorP); 
	!go(P). 
+!go(P) : atRoom(RoomAg) & atRoom(P, RoomP) & not RoomAg == RoomP <-
	.println("Owner is at ", RoomAg,", that is not a contiguous room to ", RoomP);
	move_towards(P).                                                          
-!go(P) <- .println("Something goes wrong......").
	                                                                        
	
+!get(drug) : .my_name(Name) <- 
   Time = math.ceil(math.random(4000));
   .println("I am waiting ", Time, " ms. before asking the nurse robot for my medicine.");
   .wait(Time);
   .send(enfermera, achieve, has(Name, drug)).

+!get(Med) : .my_name(Name) <- 
   Time = math.ceil(math.random(4000));
   .println("I am waiting ", Time, " ms. before asking the nurse robot for my medicine.");
   .wait(Time);
   .send(enfermera, achieve, has(Name, Med)).

+has(owner,drug) : true <-
   .println("Owner take the drug.");
   !take(drug).
-has(owner,drug) : true <-
   .println("Owner ask for drug. It is time to take it.");
   !get(drug).
                                       
// while I have drug, sip
+!take(drug) : has(owner, drug) <-
   sip(drug);
   .println("Owner is siping the drug.");
   !take(drug).
+!take(drug) : not has(owner, drug) <-
   .println("Owner has finished to take the drug.").//;
   //-asked(drug).

+!check_bored : true
   <- .random(X); .wait(X*5000+2000);  // Owner get bored randomly
      .send(enfermera, askOne, time(_), R); // when bored, owner ask the robot about the time
      .print(R);
	  .send(enfermera, tell, chat("What's the weather in Ourense?"));
      !check_bored.

+msg(M)[source(Ag)] : .my_name(Name)
   <- .print(Ag, " send ", Name, " the message: ", M);
      -msg(M).

// Planes para posicionarse junto al gabinete de medicamentos y junto a la enfermera

// Plan para posicionarse al lado del gabinete de medicamentos
+!position_beside_cabinet : next_to(owner,medcabinet) <-
    .println("Propietario ya está al lado del gabinete de medicamentos.");
    .wait(500).
+!position_beside_cabinet : not next_to(owner,medcabinet) <-
    .println("Propietario se posiciona al lado del gabinete de medicamentos");
    position_next_to(medcabinet);
    !position_beside_cabinet.

// Plan para posicionarse al lado de la enfermera
+!position_beside_nurse : next_to(owner,enfermera) <-
    .println("Propietario ya está al lado de la enfermera.");
    .wait(500).
+!position_beside_nurse : not next_to(owner,enfermera) <-
    .println("Propietario se posiciona al lado de la enfermera");
    position_next_to(enfermera);
    !position_beside_nurse.

// Plan para tomar medicación directamente desde el gabinete
+!take_own_medication(Med) <-
    .println("Propietario va a tomar su medicación ", Med, " directamente del gabinete");
    !position_beside_cabinet;
    open(medcabinet);
    take_medication(Med);
    close(medcabinet);
    .println("Propietario ha tomado su medicación ", Med);
    .send(enfermera, tell, taken_medication(Med)).

// Cuando recibe una notificación de que la enfermera entregó medicación
+medication_delivered(Med)[source(enfermera)] <-
    .println("Propietario recibió medicación ", Med, " de la enfermera");
    +has(owner, Med);
    !take(Med).

// Planes para interactuar con el gabinete de medicamentos
+next_to(owner,medcabinet) : true <-
    .println("El propietario está junto al gabinete de medicamentos.");
    !open_medcabinet.

+!open_medcabinet : true <-
    .println("El propietario está abriendo el gabinete de medicamentos.");
    open(medcabinet);
    .wait(1000);
    !take_medication.

+!take_medication : true <-
    .println("El propietario está tomando su medicación.");
    // Elegir un medicamento aleatorio
    .random(R);
    .findall(Med, medication(Med, _, _), Meds);
    .length(Meds, L);
    Index = math.floor(R * L) + 1;
    .nth(Index-1, Meds, SelectedMed);
    
    // Tomar el medicamento
    take_medication(SelectedMed);
    .println("El propietario ha tomado ", SelectedMed);
    .wait(2000);
    
    // Cerrar el gabinete
    close(medcabinet);
    .println("El propietario ha cerrado el gabinete de medicamentos.");
    
    // Notificar a la enfermera
    .send(enfermera, tell, took_medication(SelectedMed)).


//Owner cambia las pautas,para ello utiliza números aleatorios e informa al robot.
+!cambiarPauta(T)
	<-.findall(pauta(M,H,F),.belief(pauta(M,H,F)),L);
		.print("Reseteando medicinas:");
		for(.member(pauta(M,H,F),L))
		{
			if(not H==T)
			{
				.random([0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23],X);
				if(not X==T)
				{
					.abolish(pauta(M,H,F));
					.print("Nueva pauta:[",M,",",X,",",F,"]");
					+pauta(M,X,F);
					.send(robot,tell,pautaNueva(M,X,F));
				}
			}
		}.

//Cuando es de noche,el owner se va a dormir y suspende su simulación de comportamiento.El owner tambien reetearía las pautas.	
+noche : not durmiendo & hour(T) <-
	if(.intend(simulate_behaviour))
	{
		.drop_intention(simulate_behaviour);
	};
	//Cambiamos pautas
	!cambiarPauta(T);
    .random([bed3,bed2,bed1],Y);
	.print("Es de noche, voy a dormir a ",Y);
	!go_at(owner,Y);
    +durmiendo.

//Al ser de día el owner se despierta.
+dia : durmiendo
<-	-durmiendo;
	.print("He despertado");
	!simulate_behaviour.

//Simulamos comportamiento,utilizamos la aleatoriedad para distribuirlo en 3 casos:1)Moverse a elementos actuables2)Sentarse3)Siesta.
+!simulate_behaviour[source(self)] : not durmiendo
   <- .random(X); .wait(3000*X + 5000); // wait for a random time
     if(X < 0.5){
      .random([delivery,fridge,washer,retrete],Y);
      .print("Voy a un sitio ",Y);
      !go_at(owner,Y);
     }
     elif(X < 0.7){
      .random([chair1,chair2,chair3,chair4,sofa],Y);
      .print("Voy a sentarme en",Y);
      !go_at(owner,Y);
	  //sit(Y);
	  .print("Me siento");
     }else{
      .random([bed1,bed2,bed3],Y);
      .print("Voy a echarme una siesta en");
      !go_at(owner,Y);
	  .print("Me acuesto");
     }
     !simulate_behaviour.

//El owner si es hora de la pauta,tiene una probabilidad A(P(A)=0.2) de ir por la pauta.
+hour(H) : dia<-
   .random(X);
   if(X < 0.9){
   	  .print("Voy a por medicinas");
      .findall(pauta(M,H,F),.belief(pauta(M,H,F)),L);
      if(not L == []){
      if(not .intend(tomarMedicina(L)) & not quieto){
         .print(L);
         !tomarMedicina(L);
      }
     }
   }.

//El robot resuelve la condición de carrera de ir al cabinet informándole que ya que el ha llegado primero él se las dará.
+quieto
   <- .print("Espero por mis medicinas");
      if(.intend(tomarMedicina(_))){
         .drop_intention(tomarMedicina(_));
      }
      if(not .intend(simulate_behaviour) & not durmiendo){
         !simulate_behaviour;
      }.

//Desplazamiento,la coregla -go_at,sirve para indicar que no hay camino,en nuestro mundo significa que hay un agente delante por lo que a través de prioridades le pide apartar.
+!go_at(owner,P)[source(self)] : at(owner,P) <- .print("He llegado a ",P).
+!go_at(owner,P)[source(self)] : not at(owner,P)
  <- move_towards(P);
     !go_at(owner,P).
-!go_at(owner,P)[source(self)]
<- .send(robot,tell,aparta);
   !go_at(owner,P).

//Owner tiene medicina en la mano y procede a consumirla.
+has(owner,A) : true
   	<-!consume(A).

//Consumición de la medicina.
+!consume(A)[source(self)] : not .intend(consume(A))
   <- 
   .print("Voy a tomar ",A);
   consume(A);
   .wait(2000);
   .print("He tomado ",A).



// Finalización completa del plan de tomar medicamentos
// (Reemplazando la implementación parcial existente)
+!tomar(owner,L)[source(self)]
   <- !go_at(owner,cabinet);
      if(not at(robot,cabinet) & not quieto){
         open(cabinet);
		 .send(robot,achieve,comprueba(L));
         for(.member(pauta(M,H,F),L))
         {
            .abolish(pauta(M,H,F));
      	   takeDrug(owner,M);
            .print("He cogido la medicina ", M);
            .send(robot, tell, comprobarConsumo(M));
         };
         for(.member(pauta(M,H,F),L))
         {
            handDrug(M);
         }
         close(cabinet);
      }.
