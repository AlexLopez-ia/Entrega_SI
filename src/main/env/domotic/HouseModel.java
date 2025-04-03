package domotic;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import jason.environment.grid.Area;
import jason.asSyntax.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Set;

/** class that implements the Model of Domestic Robot application */
public class HouseModel extends GridWorldModel {

    // constants for the grid objects

    public static final int COLUMN  =    4;
    public static final int CHAIR  	=    8;
    public static final int SOFA  	=   16;
    public static final int FRIDGE 	=   32;
    public static final int WASHER 	=   64;
	public static final int DOOR 	=  128;                                       
	public static final int CHARGER =  256;
    public static final int TABLE  	=  512;
    public static final int BED	   	= 1024;
    public static final int WALLV   = 2048;
    public static final int MEDCABINET = 4096; // Gabinete de medicamentos

    // the grid size                                                     
    public static final int GSize = 12;        //Cells
	public final int GridSize = 1080;    //Width
                          
    private boolean fridgeOpen = false; // whether the fridge is open                                   
    private boolean cabinetOpen = false; // en lugar de medCabinetOpen
    private int carryingMedicamentos = 0; // en lugar de carryingDrug
    boolean medCabinetStateChanged = false; // whether the cabinet state has changed
    String lastMedicationTaken = null; // último medicamento tomado
    int lastMedicationQuantity = 0; // cantidad restante del último medicamento tomado
    int lastAgentToAct = -1; // último agente que realizó una acción
    int sipCount = 0; // how many sip the owner did
    private int availableMedicamentos = 2; // how many medicamentos are available
    
    private ArrayList<String> ownerMedicamentos = new ArrayList<>();
    
    private int availableParacetamol = 20;
    private int availableIbuprofeno = 20;
    private int availableLorazepam = 20;
    private int availableAspirina = 20;
    private int availableAmoxicilina = 20;

    private int deliveredParacetamol = 0;
    private int deliveredIbuprofeno = 0;
    private int deliveredLorazepam = 0;
    private int deliveredAspirina = 0;
    private int deliveredAmoxicilina = 0;
    
    private int ownerMove = 0;
    
    private List<Integer> bateriaRobots;

	// Initialization of the objects Location on the domotic home scene 
    public Location lSofa = new Location(GSize/2, GSize-2);
    public Location lChair1 = new Location(GSize/2+2, GSize-3);
    public Location lChair3 = new Location(GSize/2-1, GSize-3);
    public Location lChair2 = new Location(GSize/2+1, GSize-4); 
    public Location lChair4 = new Location(GSize/2, GSize-4); 
    public Location lWasher = new Location(GSize/3, 0);	
    public Location lFridge = new Location(2, 0);
    public Location lTable = new Location(GSize/2, GSize-3);
    public Location lVacio = new Location(GSize/2+1, GSize-3);
	public Location lBed2	= new Location(GSize+2, 0);
	public Location lBed3	= new Location(GSize*2-3,0);
	public Location lBed1	= new Location(GSize+1, GSize*3/4);
    public Location lMedCabinet = new Location(0, 3); // Movido a una posición más accesible en la cocina
    public Location lDeliver = new Location(3, 3); // Ubicación de entrega de medicamentos

	// Initialization of the doors location on the domotic home scene 
	public Location lDoorHome = new Location(0, GSize-1);  
	public Location lDoorKit1 = new Location(0, GSize/2);
	public Location lDoorKit2 = new Location(GSize/2+1, GSize/2-1); 
	public Location lDoorSal1 = new Location(GSize/4, GSize-1);  
	public Location lDoorSal2 = new Location(GSize+1, GSize/2);
	public Location lDoorBed1 = new Location(GSize-1, GSize/2);
	public Location lDoorBath1 = new Location(GSize-1, GSize/4+1);
	public Location lDoorBed3 = new Location(GSize*2-1, GSize/4+1); 	
	public Location lDoorBed2 = new Location(GSize+1, GSize/4+1); 	
	public Location lDoorBath2 = new Location(GSize*2-4, GSize/2+1); 	
	
	// Initialization of the area modeling the home rooms      
	public static final Area kitchen = new Area(0, 0, GSize/2+1, GSize/2-1);
	public static final Area livingroom = new Area(GSize/3, GSize/2+1, GSize, GSize-1);
	public static final Area bath1 = new Area(GSize/2+2, 0, GSize-1, GSize/3);
	public static final Area bath2 = new Area(GSize*2-3, GSize/2+1, GSize*2-1, GSize-1);
	public static final Area bedroom1 = new Area(GSize+1, GSize/2+1, GSize*2-4, GSize-1);
	public static final Area bedroom2 = new Area(GSize, 0, GSize*3/4-1, GSize/3);
	public static final Area bedroom3 = new Area(GSize*3/4, 0, GSize*2-1, GSize/3);
	public static final Area hall = new Area(0, GSize/2+1, GSize/4, GSize-1);
	public static final Area hallway = new Area(GSize/2+2, GSize/2-1, GSize*2-1, GSize/2);
	/*
	Modificar el modelo para que la casa sea un conjunto de habitaciones
	Dar un codigo a cada habitación y vincular un Area a cada habitación
	Identificar los objetos de manera local a la habitación en que estén
	Crear un método para la identificación del tipo de agente existente
	Identificar objetos globales que precisen de un único identificador
	*/
	
    public HouseModel() {
        // create a GSize x 2GSize grid with 2 mobile agents (robot y owner)
        super(2*GSize, GSize, 2);
        
        // initial location of robot (column 3, line 3)
        // ag code 0 means the robot
        setAgPos(0, 8, 1);  
		setAgPos(1, 5, 9);
		// setAgPos(2, 0, 0); // Comentamos esta línea ya que solo tenemos 2 agentes

		// Do a new method to create literals for each object placed on
		// the model indicating their nature to inform agents their existence
				
        // initial location of fridge and owner
        add(FRIDGE, lFridge); 
		add(WASHER, lWasher); 
		add(SOFA, lSofa);
		add(CHAIR, lChair2);
		add(CHAIR, lChair3);
		add(CHAIR, lChair4);
        add(CHAIR, lChair1);  
        add(TABLE, lTable);  
		add(BED, lBed1);
		add(BED, lBed2);
		add(BED, lBed3);
        add(MEDCABINET, lMedCabinet); // Añadir gabinete de medicamentos
        
        addWall(GSize/2+1, 0, GSize/2+1, GSize/2-2);  		
        add(DOOR, lDoorKit2);                              
        //addWall(GSize/2+1, GSize/2-1, GSize/2+1, GSize-2);  
        add(DOOR, lDoorSal1); 

        addWall(GSize/2+1, GSize/4+1, GSize-2, GSize/4+1);   
        //addWall(GSize+1, GSize/4+1, GSize*2-1, GSize/4+1);   
        add(DOOR, lDoorBath1); 
        //addWall(GSize+1, GSize*2/5+1, GSize*2-2, GSize*2/5+1);   
        addWall(GSize+2, GSize/4+1, GSize*2-2, GSize/4+1);   
        addWall(GSize*2-6, 0, GSize*2-6, GSize/4);
        add(DOOR, lDoorBed1); 
        
        addWall(GSize, 0, GSize, GSize/4+1);  
        //addWall(GSize+1, GSize/4+1, GSize, GSize/4+1);  
        add(DOOR, lDoorBed2); 
        
        addWall(1, GSize/2, GSize/2+1, GSize/2);            
        add(DOOR, lDoorKit1);                
        add(DOOR, lDoorSal2);

        addWall(GSize/4, GSize/2+1, GSize/4, GSize-2);            
        
        addWall(GSize, GSize/2, GSize, GSize-1);  
        addWall(GSize*2-4, GSize/2+2, GSize*2-4, GSize-1);  
        addWall(GSize/2+3, GSize/2, GSize, GSize/2);  
        addWall(GSize+2, GSize/2, GSize*2-1, GSize/2);  
        add(DOOR, lDoorBed3);  
        add(DOOR, lDoorBath2);  
    }
    
    // Clase interna para almacenar información de medicamentos
    class Medication {
        String name;
        int quantity;
        String schedule;
        
        public Medication(String name, int quantity, String schedule) {
            this.name = name;
            this.quantity = quantity;
            this.schedule = schedule;
        }
    }
    
    // Mapa para almacenar medicamentos
    Map<String, Medication> medications = new HashMap<>();
	
	public String getRoom(Location thing) {
		String byDefault = "kitchen";
		
		if (bath1.contains(thing)) {
			byDefault = "bath1";
		}
		if (bath2.contains(thing)) {
			byDefault = "bath2";
		}
		if (bedroom1.contains(thing)) {
			byDefault = "bedroom1";
		}
		if (bedroom2.contains(thing)) {
			byDefault = "bedroom2";
		}
		if (bedroom3.contains(thing)) {
			byDefault = "bedroom3";
		}
		if (hallway.contains(thing)) {
			byDefault = "hallway";
		}
		if (livingroom.contains(thing)) {
			byDefault = "livingroom";
		}
		if (hall.contains(thing)) {
			byDefault = "hall";
		}
		return byDefault;
	}
	
	public Location getRoomCenter(String thing) {
		Location toret = kitchen.center();
		
		if (thing.equals("bath1")) {
			toret = bath1.center();
		}
		if (thing.equals("bath2")) {
			toret = bath2.center();
		}
		if (thing.equals("bedroom1")) {
			toret = bedroom1.center();
		}
		if (thing.equals("bedroom2")) {
			toret = bedroom2.center();
		}
		if (thing.equals("bedroom3")) {
			toret = bedroom3.center();
		}
		if (thing.equals("hallway")) {
			toret = hallway.center();
		}
		if (thing.equals("livingroom")) {
			toret = livingroom.center();
		}
		if (thing.equals("hall")) {
			toret = hall.center();
		}
		return toret;
	}

    boolean sit(int Ag, Location dest) {
        Location loc = getAgPos(Ag);
        if (loc.isNeigbour(dest)) {
            setAgPos(Ag, dest);
        }
        return true;
    }

    boolean openFridge() {
        if (!fridgeOpen) {
            fridgeOpen = true;
            return true;
        } else {
            return false;
        }
    }

    boolean closeFridge() {
        if (fridgeOpen) {
            fridgeOpen = false;
            return true;
        } else {
            return false;
        }
    }   

    boolean openCabinet() {
        cabinetOpen = true;
        return true;
    }

    boolean closeCabinet() {
        cabinetOpen = false;
        return true;
    }

    boolean canMoveTo(int Ag, int x, int y) {
		// Verificar que la posición está dentro de los límites del grid
		if(!(x<0 || x>=24 || y<0 || y>=12))
		{
			if(!(x==7 && y==9))
			{
				// Verificar explícitamente si la posición está en una cama o cerca de ella
				// Las camas ocupan 2x2 celdas según el código de dibujo
				if ((x >= lBed1.x && x <= lBed1.x + 1 && y >= lBed1.y && y <= lBed1.y + 1) ||
					(x >= lBed2.x && x <= lBed2.x + 1 && y >= lBed2.y && y <= lBed2.y + 1) ||
					(x >= lBed3.x && x <= lBed3.x + 1 && y >= lBed3.y && y <= lBed3.y + 1)) {
					return false;
				}
				
				// Continuar con la lógica original
				if (Ag < 1) {
					return (isFree(x, y) && !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) &&
							!hasObject(SOFA, x, y) && !hasObject(CHAIR, x, y) && !hasObject(FRIDGE, x, y) && !hasObject(BED, x, y)
							&& !hasObject(MEDCABINET, x, y));
				} else {
					return (isFree(x, y) && !hasObject(WASHER, x, y) && !hasObject(TABLE, x, y) && !hasObject(FRIDGE, x, y)
							&& !hasObject(MEDCABINET, x, y));
				}
			}
		}
		
		return false;
	}
	
	public int getOwnerMove() {
        return ownerMove;
    }
    
    public void calculateOwnerDir(int Ag, Location dest) {
        if(Ag == 1) {
            Location ag = getAgPos(Ag);
            if(ag.x + 1 == dest.x) {
                ownerMove = 1;
            } else if(ag.x - 1 == dest.x) {
                ownerMove = 3;
            } else if(ag.y + 1 == dest.y) {
                ownerMove = 2;
            } else {
                ownerMove = 0;
            }
        }
    }

    public boolean moveTowards(int Ag, Location dest) {
        // Si el destino es null, no intentar moverse
        if (dest == null) {
            return false;
        }
        
        // Obtener la posición actual del agente
        Location agPos = getAgPos(Ag);
        
        // Calcular un movimiento directo si está adyacente o en línea recta
        if (agPos.distance(dest) == 1) {
            // Si está adyacente, moverse directamente
            calculateOwnerDir(Ag, dest);
            setAgPos(Ag, dest);
            return true;
        } else if (agPos.x == dest.x || agPos.y == dest.y) {
            // Si está en línea recta, intentar moverse un paso en esa dirección
            Location nextPos = null;
            if (agPos.x == dest.x) {
                // Mismo X, moverse en Y
                int stepY = (dest.y > agPos.y) ? 1 : -1;
                nextPos = new Location(agPos.x, agPos.y + stepY);
            } else {
                // Mismo Y, moverse en X
                int stepX = (dest.x > agPos.x) ? 1 : -1;
                nextPos = new Location(agPos.x + stepX, agPos.y);
            }
            
            if (nextPos != null && canMoveTo(Ag, nextPos.x, nextPos.y)) {
                calculateOwnerDir(Ag, nextPos);
                setAgPos(Ag, nextPos);
                return true;
            }
        }
        
        // Si lo anterior falla, probar con AStar
        AStar path = new AStar(this, Ag, agPos, dest);
        Location nextMove = path.getNextMove();
        if(nextMove != null) {
            calculateOwnerDir(Ag, nextMove);
            setAgPos(Ag, nextMove);
            return true;
        } else {
            // Intentar tratar a otros agentes como no sólidos
            for (int i = 0; i < 2; i++) { // Solo tenemos 2 agentes (0: robot, 1: owner)
                if(i != Ag) {
                    path.setNonSolidNode(getAgPos(i));
                }
            }
            nextMove = path.getNextMove();
            if(nextMove != null && isFree(nextMove)) {
                calculateOwnerDir(Ag, nextMove);
                setAgPos(Ag, nextMove);
                return true;
            }
            
            // Si todo lo anterior falla, intentar moverse en cualquier dirección libre
            if (trySimpleMove(Ag, dest)) {
                return true;
            }
        }
        return false;    
    }
    
    // Método auxiliar para intentar un movimiento simple hacia el destino
    private boolean trySimpleMove(int Ag, Location dest) {
        Location agPos = getAgPos(Ag);
        int dx = Integer.compare(dest.x, agPos.x); // -1, 0, o 1
        int dy = Integer.compare(dest.y, agPos.y); // -1, 0, o 1
        
        // Intentar moverse preferentemente en la dirección del destino
        Location[] options = new Location[4];
        options[0] = new Location(agPos.x + dx, agPos.y);
        options[1] = new Location(agPos.x, agPos.y + dy);
        options[2] = new Location(agPos.x + dx, agPos.y + dy);
        options[3] = new Location(agPos.x - dx, agPos.y - dy);
        
        for (Location option : options) {
            if (canMoveTo(Ag, option.x, option.y)) {
                calculateOwnerDir(Ag, option);
                setAgPos(Ag, option);
                return true;
            }
        }
        
        return false;
    }

    public boolean apartar(int Ag) {
        Location loc = getAgPos(Ag);
        ArrayList<Location> dirs = new ArrayList<>();    
        boolean aux;
        aux = canMoveTo(Ag, loc.x, loc.y-1) ? dirs.add(new Location(loc.x, loc.y - 1)) : false;
        aux = canMoveTo(Ag, loc.x, loc.y + 1) ? dirs.add(new Location(loc.x, loc.y + 1)) : false;
        aux = canMoveTo(Ag, loc.x - 1, loc.y) ? dirs.add(new Location(loc.x - 1, loc.y)) : false;
        aux = canMoveTo(Ag, loc.x + 1, loc.y) ? dirs.add(new Location(loc.x + 1, loc.y)) : false;

        if(dirs.size() > 0) {
            int random = (int) (Math.random() * dirs.size());
            setAgPos(Ag, dirs.get(random));
        }
        return dirs.size() > 0;
    }

    int getAvailableMedication(String medicamento) {
        int toRet = 0;
        switch (medicamento) {
            case "paracetamol":
                toRet = availableParacetamol;
                break;
            case "ibuprofeno":
                toRet = availableIbuprofeno;
                break;
            case "lorazepam":
                toRet = availableLorazepam;
                break;
            case "aspirina":
                toRet = availableAspirina;
                break;
            case "amoxicilina":
                toRet = availableAmoxicilina;
                break;
            default:
                break;
        }
        return toRet;
    }

    public void reduceAvailableMedication(String medicamento) {
        switch (medicamento) {
            case "paracetamol":
                availableParacetamol--;
                break;
            case "ibuprofeno":
                availableIbuprofeno--;
                break;
            case "lorazepam":
                availableLorazepam--;
                break;
            case "aspirina":
                availableAspirina--;
                break;
            case "amoxicilina":
                availableAmoxicilina--;
                break;
            default:
                break;
        }
    }

    public boolean takeMedication(String medicamento) {
        if (cabinetOpen && getAvailableMedication(medicamento) > 0) {
            reduceAvailableMedication(medicamento);
            carryingMedicamentos++;
            lastMedicationTaken = medicamento;
            lastMedicationQuantity = getAvailableMedication(medicamento);
            return true;
        } else {
            if (cabinetOpen) {
                System.out.println("The cabinet is opened. ");
            }
            if (getAvailableMedication(medicamento) > 0) {
                System.out.println("The cabinet has enough medication. ");
            }
            if (carryingMedicamentos == 0) {
                System.out.println("The robot is not carrying a medication. ");
            }
            return false;
        }
    }

    public boolean handInMedicamento(int ag) {
        if (carryingMedicamentos > 0) {
            carryingMedicamentos--;
            return true;
        } else {
            return false;
        }
    }

    public boolean comprobarConsumo(String medicamento, int num) {
        return getAvailableMedication(medicamento) == num - 1;
    }
    
    public Location getLocation(String loc) {
        Location dest = null;
        switch (loc) {
            case "fridge": dest = lFridge; break;
            case "cabinet": dest = lMedCabinet; break;
            case "robot": dest = getAgPos(0); break;
            case "auxiliar": dest = getAgPos(2); break;
            case "owner": dest = getAgPos(1); break;
            case "chair1": dest = lChair1; break;
            case "chair2": dest = lChair2; break;
            case "chair3": dest = lChair3; break;
            case "chair4": dest = lChair4; break;
            case "bed1": dest = lBed1; break;
            case "bed2": dest = lBed2; break;
            case "bed3": dest = lBed3; break;
            case "sofa": dest = lSofa; break;
            case "washer": dest = lWasher; break;
            case "table": dest = lTable; break;
            case "doorBed1": dest = lDoorBed1; break;
            case "doorBed2": dest = lDoorBed2; break;
            case "doorBed3": dest = lDoorBed3; break;
            case "doorKit1": dest = lDoorKit1; break;
            case "doorKit2": dest = lDoorKit2; break;
            case "doorSal1": dest = lDoorSal1; break;
            case "doorSal2": dest = lDoorSal2; break;
            case "doorBath1": dest = lDoorBath1; break;
            case "doorBath2": dest = lDoorBath2; break;
        }
        return dest;
    }

    public boolean deliver(String medicamento, int qtd) {
        switch (medicamento) {
            case "paracetamol":
                deliveredParacetamol += qtd;
                break;
            case "ibuprofeno":
                deliveredIbuprofeno += qtd;
                break;
            case "lorazepam":
                deliveredLorazepam += qtd;
                break;
            case "aspirina":
                deliveredAspirina += qtd;
                break;
            case "amoxicilina":
                deliveredAmoxicilina += qtd;
                break;
            default:
                break;
        }
        return true;
    }

    public boolean getDelivery(String medicamento) {
        switch (medicamento) {
            case "paracetamol":
                deliveredParacetamol = 0;
                break;
            case "ibuprofeno":
                deliveredIbuprofeno = 0;
                break;
            case "lorazepam":
                deliveredLorazepam = 0;
                break;
            case "aspirina":
                deliveredAspirina = 0;
                break;
            case "amoxicilina":
                deliveredAmoxicilina = 0;
                break;
            default:
                break;
        }
        return true;
    }

    public boolean addMedication(String medicamento, int qtd) {
        switch (medicamento) {
            case "paracetamol":
                availableParacetamol = qtd;
                break;
            case "ibuprofeno":
                availableIbuprofeno = qtd;
                break;
            case "lorazepam":
                availableLorazepam = qtd;
                break;
            case "aspirina":
                availableAspirina = qtd;
                break;
            case "amoxicilina":
                availableAmoxicilina = qtd;
                break;
            default:
                break;
        }
        return true;
    }
    
    // Métodos para gestión de medicamentos
    public Set<String> getMedicationNames() {
        return medications.keySet();
    }
    
    public Medication getMedicationInfo(String medName) {
        return medications.get(medName);
    }
    
    public int getAvailableParacetamol() {
        return availableParacetamol;
    }
    
    public int getAvailableIbuprofeno() {
        return availableIbuprofeno;
    }
    
    public int getAvailableLorazepam() {
        return availableLorazepam;
    }
    
    public int getAvailableAspirina() {
        return availableAspirina;
    }
    
    public int getAvailableAmoxicilina() {
        return availableAmoxicilina;
    }
    
    public List<String> getOwnerMedicamentos() {
        return ownerMedicamentos;
    }

    /**
     * Elimina un medicamento de la lista del propietario
     * @param medicamento Nombre del medicamento a eliminar
     */
    public void removeOwnerMedicamento(String medicamento) {
        ownerMedicamentos.remove(medicamento);
    }
    
    /**
     * Añade un medicamento a la lista del propietario
     * @param medicamento Nombre del medicamento a añadir
     */
    public void addOwnerMedicamento(String medicamento) {
        ownerMedicamentos.add(medicamento);
    }

    /**
     * Obtiene la cantidad de medicamentos que está transportando el robot
     */
    public int getCarryingMedicamentos() {
        return carryingMedicamentos;
    }

    /**
     * Verifica si el gabinete de medicamentos está abierto
     */
    public boolean isCabinetOpen() {
        return cabinetOpen;
    }
    
    /**
     * Obtiene el conteo de sorbos
     */
    public int getSipCount() {
        return sipCount;
    }
    
    /**
     * Obtiene el tamaño de la cuadrícula
     */
    public int getGridSize() {
        return GridSize;
    }
    
    // Getters para las ubicaciones
    public Location getlSofa() {
        return lSofa;
    }
    
    public Location getlChair1() {
        return lChair1;
    }
    
    public Location getlChair2() {
        return lChair2;
    }
    
    public Location getlChair3() {
        return lChair3;
    }
    
    public Location getlChair4() {
        return lChair4;
    }
    
    public Location getlWasher() {
        return lWasher;
    }
    
    public Location getlFridge() {
        return lFridge;
    }
    
    public Location getlTable() {
        return lTable;
    }
    
    public Location getlBed1() {
        return lBed1;
    }
    
    public Location getlBed2() {
        return lBed2;
    }
    
    public Location getlBed3() {
        return lBed3;
    }
    
    public Location getlMedCabinet() {
        return lMedCabinet;
    }
    
    public Location getlDeliver() {
        return lDoorHome; // Asumimos que la entrega es en la puerta principal
    }
}
