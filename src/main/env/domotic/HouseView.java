package domotic;

import jason.environment.grid.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.ImageIcon;

/** class that implements the View of Domestic Robot application */
public class HouseView extends GridWorldView {

    HouseModel model;
    
    
    // Para mensajes de estado
    private String statusMessage = null;
    private long statusMessageTime = 0;
    private final long MESSAGE_DURATION = 3000; // 3 segundos
    
    // Para efecto de resaltado del gabinete
    private boolean highlightCabinet = false;
    private long highlightStartTime = 0;
    private final long HIGHLIGHT_DURATION = 1000; // 1 segundo

    // Variables originales
    String currentDirectory;
    int viewSize;
    
    // Para animación del propietario
    private int ownerPass = 0;

    public HouseView(HouseModel model) {
        super(model, "Domestic Care Robot", 800);
        this.model = model;
        defaultFont = new Font("Arial", Font.BOLD, 14); // change default font
        setVisible(true);
        currentDirectory = Paths.get("").toAbsolutePath().toString();
        viewSize = 800;
        setSize(viewSize, viewSize/2);
      //  repaint();
    }

    /** draw application objects */
    @Override
    public void draw(Graphics g, int x, int y, int object) {
        Location lRobot = model.getAgPos(0);
        Location lOwner = model.getAgPos(1);
        Location lAuxiliar = model.getAgPos(2);
        Location loc = new Location(x, y);
        String objPath = currentDirectory;
        g.setColor(Color.white);
        super.drawEmpty(g, x, y);
        switch (object) {
        case HouseModel.BED:
            g.setColor(Color.lightGray);
            if (model.lBed1.equals(loc)) {   
                drawMultipleScaledImage(g, x, y, "/doc/doubleBedlt.png", 2, 2, 100, 100);
                g.setColor(Color.red);
                super.drawString(g, x, y, defaultFont, " 1 ");
            }
            if (model.lBed2.equals(loc)) {  
                objPath = "/doc/singleBed.png";
                drawMultipleScaledImage(g, x, y, objPath, 2, 2, 60, 90); 
                g.setColor(Color.red);
                super.drawString(g, x, y, defaultFont, " 2 ");
            }
            if (model.lBed3.equals(loc)) {
                objPath = "/doc/singleBed.png";
                drawMultipleScaledImage(g, x, y, objPath, 2, 2, 60, 90); 
                g.setColor(Color.red);
                super.drawString(g, x, y, defaultFont, " 3 ");
            }
            break;                                                                                                  
        case HouseModel.CHAIR:
            g.setColor(Color.lightGray);
            if (model.lChair1.equals(loc)) {              
                objPath = "/doc/chairL.png";
                drawScaledImageMd(g, x, y, objPath, 80, 80);
            }
            if (model.lChair2.equals(loc)) {  
                objPath = "/doc/chairD.png";
                drawScaledImageMd(g, x, y, objPath, 80, 80); 
            }
            if (model.lChair4.equals(loc)) {  
                objPath = "/doc/chairD.png";
                drawScaledImageMd(g, x, y, objPath, 80, 80); 
            }
            if (model.lChair3.equals(loc)) {
                objPath = "/doc/chairU.png";
                drawScaledImageMd(g, x, y, objPath, 80, 80);
            }
            break;                                                                                                  
        case HouseModel.SOFA:                                                                                      
            g.setColor(Color.lightGray);
            objPath = "/doc/sofa.png";
            drawMultipleScaledImage(g, x, y, objPath, 2, 1, 90, 90);
            break; 
        case HouseModel.TABLE:
            g.setColor(Color.lightGray);
            objPath = "/doc/table.png";
            drawMultipleScaledImage(g, x, y, objPath, 2, 1, 80, 80);
            break;              
        case HouseModel.DOOR:
            g.setColor(Color.lightGray);
            if (lRobot.equals(loc) || lRobot.isNeigbour(loc) || 
                lOwner.equals(loc) || lOwner.isNeigbour(loc)) {
                objPath = "/doc/openDoor2.png";
                drawScaledImage(g, x, y, objPath, 75, 100);
            } else {   
                objPath = "/doc/closeDoor2.png";
                drawScaledImage(g, x, y, objPath, 75, 100);				
            }           
            break;
        case HouseModel.WASHER:
            g.setColor(Color.lightGray);
            if (lRobot.equals(model.lWasher)) {
                objPath = "/doc/openWasher.png";
                drawScaledImage(g, x, y, objPath, 50, 60);
            } else {
                objPath = "/doc/closeWasher.png";
                drawImage(g, x, y, objPath);		
            }           
            break;
        case HouseModel.FRIDGE:
            g.setColor(Color.lightGray); 
            if (lRobot.isNeigbour(model.lFridge) || lOwner.isNeigbour(model.lFridge)) { 
                objPath = "/doc/openNevera.png";
                drawImage(g, x, y, objPath);
                g.setColor(Color.yellow);
            } else {   
                objPath = "/doc/closeNevera.png";
                drawImage(g, x, y, objPath);	
                g.setColor(Color.blue);
            }                      
            drawString(g, x, y, defaultFont, "Fr (" + model.getAvailableMedication("general") + ")");
            break; 
        case HouseModel.MEDCABINET:
            drawMedCabinet(g, x, y);
            break;
        case HouseModel.puntoRecogida:
            g.setColor(Color.lightGray);
            objPath = "/doc/pickup.png";
            drawImage(g, x, y, objPath);
            g.setColor(Color.blue);
            super.drawString(g, x, y, defaultFont, "P");
            break;
        }
      //  repaint();
    }
                          
    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        Location lRobot = model.getAgPos(0);
        Location lOwner = model.getAgPos(1);
        Location lAuxiliar = model.getAgPos(2);
        String objPath = currentDirectory;

        if (id == 0) { // Robot
            if (!lRobot.equals(lOwner) && !lRobot.equals(model.lFridge)) {
                c = Color.yellow;
                if (model.getCarryingMedicamentos() > 0) {
                    objPath = "/doc/beerBot.png";
                    drawImage(g, x, y, objPath);
                } else {
                    objPath = "/doc/bot.png";
                    drawImage(g, x, y, objPath);
                }
                g.setColor(Color.black);
                super.drawString(g, x, y, defaultFont, "Rob");
            }
        } else if (id == 1) { // Propietario
            if (lOwner.equals(model.lChair1)) {
                drawMan(g, model.lChair1.x, model.lChair1.y, "left"); 
            } else if (lOwner.equals(model.lChair2)) {
                drawMan(g, model.lChair2.x, model.lChair2.y, "down");
            } else if (lOwner.equals(model.lChair4)) {
                drawMan(g, model.lChair4.x, model.lChair4.y, "down");
            } else if (lOwner.equals(model.lChair3)) {      
                drawMan(g, model.lChair3.x, model.lChair3.y, "right");    
            } else if (lOwner.equals(model.lBed1)) {
                drawMan(g, model.lBed1.x, model.lBed1.y, "right");
            } else if (lOwner.equals(model.lBed2)) {
                drawMan(g, model.lBed2.x, model.lBed2.y, "down");
            } else if (lOwner.equals(model.lBed3)) {
                drawMan(g, model.lBed3.x, model.lBed3.y, "down");
            } else if (lOwner.equals(model.lSofa)) {
                drawMan(g, model.lSofa.x, model.lSofa.y, "up");    
            } else if (lOwner.equals(model.lDoorHome)) {
                g.setColor(Color.lightGray); 
                objPath = "/doc/openDoor2.png";
                drawScaledImage(g, x, y, objPath, 75, 100);
                drawMan(g, x, y, "down");
            } else {
                // Sistema de animación para el propietario con diferentes direcciones de movimiento
                switch(model.getOwnerMove()) {
                    case 0: // Arriba
                        if(ownerPass == 0) {
                            drawMan(g, x, y, "walklu");
                        } else {
                            drawMan(g, x, y, "walkru");
                        }
                        break;
                    case 1: // Derecha
                        if(ownerPass == 0) {
                            drawMan(g, x, y, "walklr");
                        } else {
                            drawMan(g, x, y, "walkrr");
                        }
                        break;
                    case 2: // Abajo
                        if(ownerPass == 0) {
                            drawMan(g, x, y, "walkld");
                        } else {
                            drawMan(g, x, y, "walkrd");
                        }
                        break;
                    case 3: // Izquierda
                        if(ownerPass == 0) {
                            drawMan(g, x, y, "walkll");
                        } else {
                            drawMan(g, x, y, "walkrl");
                        }
                        break;
                    default:
                        drawMan(g, x, y, "walkr");
                }
                // Alternar entre los dos frames de animación
                ownerPass = (ownerPass == 1) ? 0 : 1;
            }
            if (lRobot != null && lRobot.isNeigbour(lOwner)) {	
                String o = "S";
                g.setColor(Color.yellow);
                drawString(g, x, y, defaultFont, o);
            }                                                           
        } else if (id == 2) { // Auxiliar
            // Visualización del agente auxiliar
            objPath = "/doc/auxiliar.png";  // Asegúrate de tener esta imagen en tu directorio de recursos
            drawImage(g, x, y, objPath);
            g.setColor(Color.blue);
            super.drawString(g, x, y, defaultFont, "Aux");
            
            // Mostrar estado de actividad si está cerca del gabinete o punto de recogida
            if (lAuxiliar.isNeigbour(model.lMedCabinet)) {
                g.setColor(Color.green);
                drawString(g, x, y, defaultFont, "R");
            } else if (lAuxiliar.isNeigbour(model.lPickup)) {
                g.setColor(Color.orange);
                drawString(g, x, y, defaultFont, "C");
            }
        }			        
    } 
	
    public void drawMultipleObstacleH(Graphics g, int x, int y, int NCells) {
        for (int i = x; i < x+NCells; i++) {
            drawObstacle(g, i, y); 
        }    
    }

    public void drawMultipleObstacleV(Graphics g, int x, int y, int NCells) {
        for (int j = y; j < y+NCells; j++) {
            drawObstacle(g, x, j);
        }    
    }

    public void drawMultipleImage(Graphics g, int x, int y, String imageAddress, int NW, int NH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + 2, y * cellSizeH + 2, NW*cellSizeW - 4, NH*cellSizeH - 4, null);
    }

    public void drawMultipleScaledImage(Graphics g, int x, int y, String imageAddress, int NW, int NH, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + NW*cellSizeW*(100-scaleW)/200, y * cellSizeH + NH*cellSizeH*(100-scaleH)/200 + 1, NW*cellSizeW*scaleW/100, NH*scaleH*cellSizeH/100, null);
    }
	
    public void drawScaledImage(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image!"+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + cellSizeH*(100-scaleH)/100, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    public void drawScaledImageUp(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + 2, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }
	
    public void drawScaledImageLf(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }
	
    public void drawScaledImageRt(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/100, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }
	
    public void drawScaledImageMd(Graphics g, int x, int y, String imageAddress, int scaleW, int scaleH) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+imageAddress);
        else 
            Img = new ImageIcon(getClass().getResource(imageAddress)); 
        g.setColor(Color.lightGray);
        g.drawImage(Img.getImage(), x * cellSizeW + cellSizeW*(100-scaleW)/200, y * cellSizeH + cellSizeH*(100-scaleH)/200 + 1, cellSizeW*scaleW/100, scaleH*cellSizeH/100, null);
    }

    public void drawImage(Graphics g, int x, int y, String imageAddress) {
        URL url = getClass().getResource(imageAddress);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! " + imageAddress);
        else
            Img = new ImageIcon(getClass().getResource(imageAddress));
        g.drawImage(Img.getImage(), x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2, null);
    }
	
    public void drawMan(Graphics g, int x, int y, String how) { 
        String resource = "/doc/sitd.png";
        switch (how) {
            case "right": 
                resource = "/doc/sitr.png"; 
                break;
            case "left": 
                resource = "/doc/sitl.png";  
                break;     
            case "up": 
                resource = "/doc/situ.png";  
                break;     
            case "down": 
                resource = "/doc/sitd.png"; 
                break;
            case "stand": 
                resource = "/doc/sits.png"; 
                break;
            case "walkr": 
                resource = "/doc/walklr.png"; 
                break;
            // Nuevos casos para las animaciones de movimiento
            case "walkrr": 
                resource = "/doc/walkrr.png"; 
                break;
            case "walklr": 
                resource = "/doc/walklr.png"; 
                break;
            case "walklu": 
                resource = "/doc/walklu.png"; 
                break;
            case "walkru": 
                resource = "/doc/walkru.png"; 
                break;
            case "walkld": 
                resource = "/doc/walkld.png"; 
                break;
            case "walkrd": 
                resource = "/doc/walkrd.png"; 
                break;
            case "walkll": 
                resource = "/doc/walkll.png"; 
                break;
            case "walkrl": 
                resource = "/doc/walkrl.png"; 
                break;
        }
        URL url = getClass().getResource(resource);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+resource);
        else 
            Img = new ImageIcon(getClass().getResource(resource)); 
        g.drawImage(Img.getImage(), x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 3, cellSizeH - 3, null);
    }
        
    public void drawManSittingRight(Graphics g, int x, int y) {
        String objPath = "/doc/sitr.png";
        URL url = getClass().getResource(objPath);
        ImageIcon Img = new ImageIcon();
        if (url == null)
            System.out.println("Could not find image! "+objPath);
        else 
            Img = new ImageIcon(getClass().getResource(objPath)); 
        g.drawImage(Img.getImage(), x * cellSizeW - 4, y * cellSizeH + 1, cellSizeW + 2, cellSizeH - 2, null);
    }
        
    public void drawSquare(Graphics g, int x, int y) {
        g.setColor(Color.blue);
        g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        g.setColor(Color.cyan);
        g.drawRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 3, cellSizeH - 3);   
    }   
	
    public void drawMedCabinet(Graphics g, int x, int y) {
        // Obtener las posiciones de los agentes
        Location lRobot = model.getAgPos(0);
        Location lOwner = model.getAgPos(1);
        
        boolean isCabinetOpen = model.isCabinetOpen();
        
        if (isCabinetOpen) {
            // Gabinete abierto
            String objPath = "/doc/medicinas_abierto.jpeg";
            drawImage(g, x, y, objPath);
            g.setColor(Color.yellow);
            
            
            // Mostrar información detallada de medicamentos
            drawString(g, x, y, defaultFont, "PA (" + model.getAvailableMedication("paracetamol") + ")" + 
                       "IB (" + model.getAvailableMedication("ibuprofeno") + ")" + 
                       "LO (" + model.getAvailableMedication("lorazepam") + ")");
            drawString(g, x, y + 1, defaultFont, "AS (" + model.getAvailableMedication("aspirina") + ")" + 
                       "FT (" + model.getAvailableMedication("fent") + ")");
                       
          
        } else {
            // Gabinete cerrado
            String objPath = "/doc/medicinas_cerrado.jpeg";
            drawImage(g, x, y, objPath);
            g.setColor(Color.blue);
        }
    }
    
}