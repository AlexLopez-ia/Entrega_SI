package domotic;

import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;
import java.util.Map;
import javax.swing.JFrame;

public class HouseEnv extends Environment implements CalendarListener {
    //Comentario prueba
    // common literals
    public static final Literal of   = Literal.parseLiteral("open(fridge)");
    public static final Literal clf  = Literal.parseLiteral("close(fridge)");
    public static final Literal gb   = Literal.parseLiteral("get(medicamento)");
    public static final Literal hb   = Literal.parseLiteral("hand_in(medicamento)");
    public static final Literal hob  = Literal.parseLiteral("has(owner,medicamento)");

    public static final Literal af   = Literal.parseLiteral("at(robot,fridge)");
    public static final Literal ao   = Literal.parseLiteral("at(robot,owner)");
	
    public static final Literal oaf  = Literal.parseLiteral("at(owner,fridge)");
    public static final Literal oac1 = Literal.parseLiteral("at(owner,chair1)");
    public static final Literal oac2 = Literal.parseLiteral("at(owner,chair2)");
    public static final Literal oac3 = Literal.parseLiteral("at(owner,chair3)");
    public static final Literal oac4 = Literal.parseLiteral("at(owner,chair4)");
    public static final Literal oasf = Literal.parseLiteral("at(owner,sofa)");

    // Literales para el gabinete de medicamentos (renombrado a cabinet para compatibilidad)
    public static final Literal ac = Literal.parseLiteral("at(robot,cabinet)");
    public static final Literal oac = Literal.parseLiteral("at(owner,cabinet)");
    public static final Literal oc = Literal.parseLiteral("open(cabinet)");
    public static final Literal clc = Literal.parseLiteral("close(cabinet)");
    
    // Literales para ubicaciones adicionales
    public static final Literal ar = Literal.parseLiteral("at(robot,retrete)");
    public static final Literal or = Literal.parseLiteral("at(owner,retrete)");
    public static final Literal aw = Literal.parseLiteral("at(robot,washer)");
    public static final Literal oaw = Literal.parseLiteral("at(owner,washer)");
    public static final Literal oab1 = Literal.parseLiteral("at(owner,bed1)");
    public static final Literal oab2 = Literal.parseLiteral("at(owner,bed2)");
    public static final Literal oab3 = Literal.parseLiteral("at(owner,bed3)");
    
    // Literales para puertas
    public static final Literal adbat1 = Literal.parseLiteral("at(robot,lDoorBath1)");
    public static final Literal adbat2 = Literal.parseLiteral("at(robot,lDoorBath2)");
    public static final Literal adb1 = Literal.parseLiteral("at(robot,lDoorBed1)");
    public static final Literal adb2 = Literal.parseLiteral("at(robot,lDoorBed2)");
    public static final Literal adb3 = Literal.parseLiteral("at(robot,lDoorBed3)");
    public static final Literal adk1 = Literal.parseLiteral("at(robot,lDoorKit1)");
    public static final Literal adk2 = Literal.parseLiteral("at(robot,lDoorKit2)");
    public static final Literal ads1 = Literal.parseLiteral("at(robot,lDoorSal1)");
    public static final Literal ads2 = Literal.parseLiteral("at(robot,lDoorSal2)");
    public static final Literal adh = Literal.parseLiteral("at(robot,lDoorHome)");

    // Literales para estar exactamente al lado del gabinete
    public static final Literal next_to_c_robot = Literal.parseLiteral("next_to(robot,cabinet)");
    public static final Literal next_to_c_owner = Literal.parseLiteral("next_to(owner,cabinet)");

    // Literal para cuando los agentes están uno al lado del otro
    public static final Literal next_to_each_other = Literal.parseLiteral("next_to(robot,owner)");
    public static final Literal next_to_owner_robot = Literal.parseLiteral("next_to(owner,robot)");

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());
    
    // ownerMedicamentos gestionado en HouseModel
    
    // Objeto Calendar para gestionar el tiempo
    private Calendar calendar;

    HouseModel model; // the model of the grid

    @Override
    public void init(String[] args) {
        model = new HouseModel();
        try {
            calendar = new Calendar(this); // Inicialización del calendario
        } catch (Exception e) {
            System.out.println("Error al inicializar el calendario: " + e.getMessage());
            e.printStackTrace();
        }
        // Inicialización de ownerMedicines
        model.getOwnerMedicamentos();

        if (args.length == 1 && args[0].equals("gui")) {
            HouseView view = new HouseView(model);
            model.setView(view);
        }

        updatePercepts();
    }
	
    void updateAgentsPlace() {
        // get the robot location
        Location lRobot = model.getAgPos(0);
        // get the robot room location
        String RobotPlace = model.getRoom(lRobot);
        addPercept("robot", Literal.parseLiteral("atRoom("+RobotPlace+")"));
        addPercept("owner", Literal.parseLiteral("atRoom(robot,"+RobotPlace+")"));
        // get the owner location
        Location lOwner = model.getAgPos(1);
        // get the owner room location
        String OwnerPlace = model.getRoom(lOwner);
        addPercept("owner", Literal.parseLiteral("atRoom("+OwnerPlace+")"));  
        addPercept("robot", Literal.parseLiteral("atRoom(owner,"+OwnerPlace+")"));
        
        // Verificar si los agentes están en puertas
        if (lRobot.distance(model.lDoorHome)==0 ||
            lRobot.distance(model.lDoorKit1)==0 ||
            lRobot.distance(model.lDoorKit2)==0 ||
            lRobot.distance(model.lDoorSal1)==0 ||
            lRobot.distance(model.lDoorSal2)==0 ||
            lRobot.distance(model.lDoorBath1)==0 ||
            lRobot.distance(model.lDoorBath2)==0 ||
            lRobot.distance(model.lDoorBed1)==0 ||
            lRobot.distance(model.lDoorBed2)==0 ||
            lRobot.distance(model.lDoorBed3)==0) {
            addPercept("robot", Literal.parseLiteral("atDoor"));
        }
        
        if (lOwner.distance(model.lDoorHome)==0 ||
            lOwner.distance(model.lDoorKit1)==0 ||
            lOwner.distance(model.lDoorKit2)==0 ||
            lOwner.distance(model.lDoorSal1)==0 ||
            lOwner.distance(model.lDoorSal2)==0 ||
            lOwner.distance(model.lDoorBath1)==0 ||
            lOwner.distance(model.lDoorBath2)==0 ||
            lOwner.distance(model.lDoorBed1)==0 ||
            lOwner.distance(model.lDoorBed2)==0 ||
            lOwner.distance(model.lDoorBed3)==0) {
            addPercept("owner", Literal.parseLiteral("atDoor"));
        }
    }
      
    void updateThingsPlace() {
        // get the fridge location
        String fridgePlace = model.getRoom(model.lFridge);
        addPercept(Literal.parseLiteral("atRoom(fridge, "+fridgePlace+")"));
        String sofaPlace = model.getRoom(model.lSofa);
        addPercept(Literal.parseLiteral("atRoom(sofa, "+sofaPlace+")")); 
        String chair1Place = model.getRoom(model.lChair1);
        addPercept(Literal.parseLiteral("atRoom(chair1, "+chair1Place+")"));
        String chair2Place = model.getRoom(model.lChair2);
        addPercept(Literal.parseLiteral("atRoom(chair2, "+chair2Place+")"));
        String chair3Place = model.getRoom(model.lChair3);
        addPercept(Literal.parseLiteral("atRoom(chair3, "+chair3Place+")"));
        String chair4Place = model.getRoom(model.lChair4);
        addPercept(Literal.parseLiteral("atRoom(chair4, "+chair4Place+")"));
        
        
        // Actualizar ubicaciones de camas
        String bed1Place = model.getRoom(model.lBed1);
        addPercept(Literal.parseLiteral("atRoom(bed1, "+bed1Place+")"));
        String bed2Place = model.getRoom(model.lBed2);
        addPercept(Literal.parseLiteral("atRoom(bed2, "+bed2Place+")"));
        String bed3Place = model.getRoom(model.lBed3);
        addPercept(Literal.parseLiteral("atRoom(bed3, "+bed3Place+")"));
        
        // Actualizar ubicación de lavadora
        String washerPlace = model.getRoom(model.lWasher);
        addPercept(Literal.parseLiteral("atRoom(washer, "+washerPlace+")"));
        
        // Ubicación del gabinete
        String cabinetPlace = model.getRoom(model.lMedCabinet);
        addPercept(Literal.parseLiteral("atRoom(cabinet, "+cabinetPlace+")"));
    }
	                                                       
    /** creates the agents percepts based on the HouseModel */
    void updatePercepts() {
        // clear the percepts of the agents
        clearPercepts("robot");
        clearPercepts("owner");
        
        updateAgentsPlace();
        updateThingsPlace(); 
        
        Location lRobot = model.getAgPos(0);
        Location lOwner = model.getAgPos(1);
        
        // Verificar distancias a diferentes ubicaciones
        if (lRobot.distance(model.lFridge)<2) {
            addPercept("robot", af);
        } 
        
        if (lOwner.distance(model.lFridge)<2) {
            addPercept("owner", oaf);
        } 
        
        // Verificar si los agentes están cerca del gabinete
        if (lRobot.distance(model.lMedCabinet) == 1) {
            addPercept("robot", next_to_c_robot);
        }
        
        if (lOwner.distance(model.lMedCabinet) == 1) {
            addPercept("owner", next_to_c_owner);
        }
        
        if (lRobot.distance(model.lMedCabinet) < 2) {
            addPercept("robot", ac);
        }
        
        if (lOwner.distance(model.lMedCabinet) < 2) {
            addPercept("owner", oac);
        }
        
        // Verificar distancia entre robot y owner
        if (lRobot.distance(lOwner)==1) {                                                     
            addPercept("robot", ao);
            addPercept("owner", next_to_owner_robot);
        }

        
        // Verificar posición en el gabinete
        if (lRobot.distance(model.lMedCabinet)==0) {
            addPercept("robot", ac);
            System.out.println("[robot] está en el gabinete.");
        }
        
        // Verificar posición del owner en diferentes muebles
        if (lOwner.distance(model.lChair1)==0) {
            addPercept("owner", oac1);
            System.out.println("[owner] is at Chair1.");
        }

        if (lOwner.distance(model.lChair2)==0) {
            addPercept("owner", oac2);
            System.out.println("[owner] is at Chair2.");
        }

        if (lOwner.distance(model.lChair3)==0) {
            addPercept("owner", oac3);
            System.out.println("[owner] is at Chair3.");
        }

        if (lOwner.distance(model.lChair4)==0) {                            
            addPercept("owner", oac4);
            System.out.println("[owner] is at Chair4.");
        }
                                                                               
        if (lOwner.distance(model.lSofa)==0) {
            addPercept("owner", oasf);
            System.out.println("[owner] is at Sofa.");
        }
        
        // Verificar posición del owner en camas
        if (lOwner.distance(model.lBed1) == 0) {
            addPercept("owner", oab1);
            System.out.println("[owner] is at Bed1.");
        }
        
        if (lOwner.distance(model.lBed2) == 0) {
            addPercept("owner", oab2);
            System.out.println("[owner] is at Bed2.");
        }
        
        if (lOwner.distance(model.lBed3) == 0) {
            addPercept("owner", oab3);
            System.out.println("[owner] is at Bed3.");
        }
        
        // Verificar posición en lavadora
        if (lRobot.distance(model.lWasher) < 2) {
            addPercept("robot", aw);
            System.out.println("[robot] is at Washer.");
        }
        
        if (lOwner.distance(model.lWasher) < 2) {
            addPercept("owner", oaw);
            System.out.println("[owner] is at Washer.");
        }


        // Añadir información sobre día y noche basada en la hora
        int hour = calendar.getHora();
        addPercept("robot", Literal.parseLiteral("hour(" + hour + ")"));
        addPercept("owner", Literal.parseLiteral("hour(" + hour + ")"));
        
        if (hour < 8 || hour >= 22) {
            addPercept("robot", Literal.parseLiteral("noche"));
            addPercept("owner", Literal.parseLiteral("noche"));
        } else {
            addPercept("robot", Literal.parseLiteral("dia"));
            addPercept("owner", Literal.parseLiteral("dia"));
        }
        
    
        // Añadir medicamentos que tiene el propietario
        for (String medicamento : model.getOwnerMedicamentos().keySet()) {
            Literal hasMedicamento = Literal.parseLiteral("has(owner," + medicamento + ")");
            addPercept("robot", hasMedicamento);
            addPercept("owner", hasMedicamento);
        }
        
        // Añadir perceptos dinámicos de cantidades de medicamentos
        for (Map.Entry<String,Integer> e : model.getAvailableMedicines().entrySet()) {
            Literal drugQty = Literal.parseLiteral("cantidad("+e.getKey()+","+e.getValue()+")");
            addPercept("robot", drugQty);
        }
        
        // Añadir pautas de medicamentos (horario y frecuencia)
        for (Map.Entry<String, HouseModel.Pauta> e : model.getPautas().entrySet()) {
            String m = e.getKey();
            int h = e.getValue().hora;
            int f = e.getValue().freq;
            addPercept("owner", Literal.parseLiteral("pauta(" + m + "," + h + "," + f + ")"));
        }
        
        // Verificar posición en puertas
        if (lRobot.distance(model.lDoorBath1) == 0) {
            addPercept("robot", adbat1);
        }
        if (lRobot.distance(model.lDoorBath2) == 0) {
            addPercept("robot", adbat2);
        }
        if (lRobot.distance(model.lDoorBed1) == 0) {
            addPercept("robot", adb1);
        }
        if (lRobot.distance(model.lDoorBed2) == 0) {
            addPercept("robot", adb2);
        }
        if (lRobot.distance(model.lDoorBed3) == 0) {
            addPercept("robot", adb3);
        }
        if (lRobot.distance(model.lDoorKit1) == 0) {
            addPercept("robot", adk1);
        }
        if (lRobot.distance(model.lDoorKit2) == 0) {
            addPercept("robot", adk2);
        }
        if (lRobot.distance(model.lDoorSal1) == 0) {
            addPercept("robot", ads1);
        }
        if (lRobot.distance(model.lDoorSal2) == 0) {
            addPercept("robot", ads2);
        }
        if (lRobot.distance(model.lDoorHome) == 0) {
            addPercept("robot", adh);
        }
    }


    @Override
    public boolean executeAction(String agName, Structure action) {
        boolean result = false;
        
        try {

            // Continuar con la lógica original
            if (agName.equals("robot")) {
                result = executerobotAction(action);
            } else if (agName.equals("owner")) {
                result = executeOwnerAction(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (result) {
            updatePercepts();
            try {
                Thread.sleep(200); // Esperar después de cada acción
            } catch (Exception e) {}
        }
        return result;
    }
    
    private boolean executerobotAction(Structure action) {
        boolean result = false;
        
        System.out.println("["+ "robot" +"] doing: "+action); 
        
        if (action.equals(of)) { // of = open(fridge)
            result = model.openFridge();
        } else if (action.equals(clf)) { // clf = close(fridge)
            result = model.closeFridge();
        } else if (action.equals(oc)) { // oc = open(cabinet)
            result = model.openCabinet();
        } else if (action.equals(clc)) { // clc = close(cabinet)
            result = model.closeCabinet();
        } else if (action.getFunctor().equals("move_towards")) {
            String l = action.getTerm(0).toString();
            Location dest = getLocationFromTerm(l);
            if (dest != null) {
                result = model.moveTowards(0, dest);
            }
        } else if (action.getFunctor().equals("apartar")) {
            result = model.apartar(0); // Implementar este método en HouseModel
        } else if (action.getFunctor().equals("takeMedication")) {
            String medicamento = action.getTerm(1).toString();
            result = model.takeMedication(0,medicamento);
        } else if (action.getFunctor().equals("handInMedicamento")) {
            String medicamento = action.getTerm(0).toString();
            result = model.handInMedicamento(0);
            model.addOwnerMedicamento(medicamento); // Añadir a los medicamentos del owner
        } else if (action.getFunctor().equals("comprobarConsumo")) {
            String medicamento = action.getTerm(0).toString();
            int num = Integer.parseInt(action.getTerm(1).toString());
            result = model.comprobarConsumo(medicamento, num);
        } 
        
        return result;
    }
    
    private boolean executeOwnerAction(Structure action) {
        boolean result = false;
        
        System.out.println("["+ "owner" +"] doing: "+action);
        
        if (action.getFunctor().equals("sit")) {
            String l = action.getTerm(0).toString();
            Location dest = null;
            switch (l) {
                case "chair1": dest = model.lChair1; break;
                case "chair2": dest = model.lChair2; break;
                case "chair3": dest = model.lChair3; break;
                case "chair4": dest = model.lChair4; break;
                case "sofa": dest = model.lSofa; break;
                case "bed1": dest = model.lBed1; break;
                case "bed2": dest = model.lBed2; break;
                case "bed3": dest = model.lBed3; break;
            }
            if (dest != null) {
                result = model.sit(1, dest);
            }
        } else if (action.equals(of)) {
            result = model.openFridge();
        } else if (action.equals(clf)) {
            result = model.closeFridge();
        } else if (action.equals(oc)) {
            result = model.openCabinet();
        } else if (action.equals(clc)) {
            result = model.closeCabinet();
        } else if (action.getFunctor().equals("takeMedication")) {
            String medicamento = action.getTerm(1).toString();
            result = model.takeMedication(1,medicamento);
        } else if (action.getFunctor().equals("handInMedicamento")) {
            String medicamento = action.getTerm(0).toString();
            result = model.handInMedicamento(1);
            model.addOwnerMedicamento(medicamento);  // registrar el medicamento en la lista del owner
        }else if (action.getFunctor().equals("move_towards")) {
            String l = action.getTerm(0).toString();
            Location dest = getLocationFromTerm(l);
            if (dest != null) {
                result = model.moveTowards(1, dest);
            }
        } else if (action.getFunctor().equals("consume")) {
            String l = action.getTerm(0).toString();
            model.removeOwnerMedicamento(l); 
            result = true;
        }
        
        return result;
    }
    
    // Método para obtener ubicación a partir de un término
    private Location getLocationFromTerm(String term) {
        Location dest = null;
        switch (term) {
            case "fridge": dest = model.lFridge; break;
            case "owner": dest = model.getAgPos(1); break;
            case "robot": dest = model.getAgPos(0); break;
            case "chair1": dest = model.lChair1; break;
            case "chair2": dest = model.lChair2; break;
            case "chair3": dest = model.lChair3; break;
            case "chair4": dest = model.lChair4; break;
            case "sofa": dest = model.lSofa; break;
            case "washer": dest = model.lWasher; break;
            case "cabinet": dest = model.lMedCabinet; break;
            case "bed1": dest = model.lBed1; break;
            case "bed2": dest = model.lBed2; break;
            case "bed3": dest = model.lBed3; break;
            case "doorBath1": dest = model.lDoorBath1; break;
            case "doorBath2": dest = model.lDoorBath2; break;
            case "doorBed1": dest = model.lDoorBed1; break;
            case "doorBed2": dest = model.lDoorBed2; break;
            case "doorBed3": dest = model.lDoorBed3; break;
            case "doorKit1": dest = model.lDoorKit1; break;
            case "doorKit2": dest = model.lDoorKit2; break;
            case "doorSal1": dest = model.lDoorSal1; break;
            case "doorSal2": dest = model.lDoorSal2; break;
            case "doorHome": dest = model.lDoorHome; break;
        }
        return dest;
    }
    
    // Métodos para gestionar los medicamentos del owner
    public Map<String,Integer> getOwnerMedicamentos() {
        return model.getOwnerMedicamentos();
    }
    
    public void addMedicamento(String medicamento, int quantity) {
        model.addMedication(medicamento, quantity);
    }
    
    // Método para obtener la hora actual del calendario
    public int getCurrentHour() {
        return calendar.getHora();
    }

    // Implementación de CalendarListener
    @Override
    public void updateTime(int hour) {
        updatePercepts();
    }
}
