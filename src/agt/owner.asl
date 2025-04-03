//Codigo para el funcionamiento del agente Owner

!pauta_medicamentos.
!simularComportamiento.
//Pautas de los medicamentos,es decir,horarios y frecuencia de toma.
pauta(paracetamol,10,6).
pauta(ibuprofeno,12,6).
pauta(lorazepam,22,23).
pauta(aspirina,17,8).
pauta(fent,15,2).


//El robot es quien controla los horarios,es decir,actualiza tras una consumicion,por lo tanto debe indicarle al owner la nueva hora.
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
   if(.intend(simularComportamiento)){
      .drop_intention(simularComportamiento);
   }
   !tomar(owner,L);
   !simularComportamiento.

//Cuando el robot le da las medicinas en la mano,le pide al owner que se quede quieto.
+espera :not durmiendo<- 
	if(.intend(simularComportamiento))
	{
		.drop_intention(simularComportamiento);
		!simularComportamiento;
	};
	.abolish(espera).



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
					.send(robot,tell,nuevaPauta(M,X,F));
				}
			}
		}.

//Cuando es de noche,el owner se va a dormir y suspende su simulación de comportamiento.El owner tambien reetearía las pautas.	


//Simulamos comportamiento,utilizamos la aleatoriedad para distribuirlo en 3 casos:1)Moverse a elementos actuables2)Sentarse3)Siesta.
+!simularComportamiento[source(self)] : not durmiendo
   <- .random(X); .wait(3000*X + 5000); // wait for a random time
     if(X < 0.5){
      .random([fridge,washer],Y);
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
     !simularComportamiento.

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

+noche : not durmiendo & hour(T) <-
	if(.intend(simularComportamiento))
	{
		.drop_intention(simularComportamiento);
	};
	//Cambiamos pautas
	!cambiarPauta(T);
    .random([bed3,bed2,bed1],Y);
	.print("Es de noche, voy a dormir a ",Y);
	!go_at(owner,Y);
    +durmiendo.

//Al ser de dia el owner se despierta.
+dia : durmiendo
<-	-durmiendo;
	.print("He despertado");
	!simularComportamiento.
//El robot resuelve la condición de carrera de ir al cabinet informándole que ya que el ha llegado primero él se las dará.
+quieto
   <- .print("Espero por mis medicinas");
      if(.intend(tomarMedicina(_))){
         .drop_intention(tomarMedicina(_));
      }
      if(not .intend(simularComportamiento) & not durmiendo){
         !simularComportamiento;
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
      	   takeMedication(owner,M);
            .print("He cogido la medicina ", M);
            .send(robot, tell, comprobarConsumo(M));
         };
         for(.member(pauta(M,H,F),L))
         {
            handInMedicamento(M);
         }
         close(cabinet);
      }.
