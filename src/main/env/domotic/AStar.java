package domotic;

import java.util.ArrayList;
import jason.environment.grid.Location;

public class AStar {

    final int maxCol;
    final int maxRow;

    Node[][] node;
    Node startNode, goalNode, currentNode;
    ArrayList<Node> openList = new ArrayList<>();
    ArrayList<Node> checkedList = new ArrayList<>();

    ArrayList<Location> path = new ArrayList<>();

    boolean goalReached = false;
    int step = 0;

    HouseModel model;
    int agentId;

    public AStar(HouseModel model, int ag, Location inicial, Location dest) {
        this.model = model;
        this.agentId = ag;
        maxCol = HouseModel.GSize;
        maxRow = HouseModel.GSize * 2;
        node = new Node[maxCol][maxRow];
        int col = 0;
        int row = 0;
        while (col < maxCol && row < maxRow) {
            node[col][row] = new Node(col, row);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
        setStartNode(inicial);
        setGoalNode(dest);
        
        // Marcar nodos sólidos basados en canMoveTo
        for (int i = 0; i < maxCol; i++) {
            for (int j = 0; j < maxRow; j++) {
                if (!model.canMoveTo(ag, j, i) && 
                    !new Location(j, i).equals(inicial) && 
                    !new Location(j, i).equals(dest)) {
                    setSolidNode(new Location(j, i));
                }
            }
        }
        
        setCostOnNodes();
        autoSearch();
    }

    private void setStartNode(Location inicial) {
        // Asegurarse de que las coordenadas están dentro de los límites
        if (inicial.y < maxCol && inicial.x < maxRow) {
            node[inicial.y][inicial.x].setAsStart();
            startNode = node[inicial.y][inicial.x];
            currentNode = startNode;
        }
    }

    private void setGoalNode(Location dest) {
        // Asegurarse de que las coordenadas están dentro de los límites
        if (dest.y < maxCol && dest.x < maxRow) {
            node[dest.y][dest.x].setAsGoal();
            goalNode = node[dest.y][dest.x];
        }
    }

    private void setSolidNode(Location loc) {
        // Asegurarse de que las coordenadas están dentro de los límites
        if (loc.y < maxCol && loc.x < maxRow) {
            node[loc.y][loc.x].setAsSolid();
        }
    }

    public void setNonSolidNode(Location loc) {
        // Asegurarse de que las coordenadas están dentro de los límites
        if (loc.y < maxCol && loc.x < maxRow) {
            node[loc.y][loc.x].setAsNonSolid();
            setCostOnNodes();
            autoSearch();
        }
    }

    private void setCostOnNodes() {
        int col = 0;
        int row = 0;
        while (col < maxCol && row < maxRow) {
            getCost(node[col][row]);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
    }

    private void getCost(Node node) {
        // Distancia Manhattan desde el nodo inicial
        int xDistance = Math.abs(node.col - startNode.col);
        int yDistance = Math.abs(node.row - startNode.row);
        node.gCost = xDistance + yDistance;

        // Distancia Manhattan hasta el nodo objetivo
        xDistance = Math.abs(node.col - goalNode.col);
        yDistance = Math.abs(node.row - goalNode.row);
        node.hCost = xDistance + yDistance;

        // Costo total
        node.fCost = node.gCost + node.hCost;
    }

    public void autoSearch() {
        // Reiniciar búsqueda
        path.clear();
        openList.clear();
        checkedList.clear();
        goalReached = false;
        step = 0;
        
        // Si los nodos inicio y destino son iguales, no hay necesidad de buscar
        if (startNode.col == goalNode.col && startNode.row == goalNode.row) {
            goalReached = true;
            return;
        }
        
        // Añadir el nodo inicial a la lista abierta
        openList.add(startNode);
        
        // Comenzar la búsqueda
        while (!goalReached && step < 300 && !openList.isEmpty()) {
            step++;
            
            // Encontrar el mejor nodo en la lista abierta
            int bestNodeIndex = getBestNodeIndex();
            
            // Si no hay nodos disponibles, terminar la búsqueda
            if (bestNodeIndex == -1) {
                break;
            }
            
            // Establecer el nodo actual como el mejor
            currentNode = openList.get(bestNodeIndex);
            
            // Marcar el nodo actual como revisado
            currentNode.setAsChecked();
            checkedList.add(currentNode);
            openList.remove(currentNode);
            
            // Si es el nodo objetivo, terminar la búsqueda
            if (currentNode == goalNode) {
                goalReached = true;
                trackPath();
                break;
            }
            
            // Abrir los nodos vecinos (4 direcciones)
            int col = currentNode.col;
            int row = currentNode.row;
            
            // Norte
            if (row - 1 >= 0)
                openNode(node[col][row - 1]);
            // Oeste
            if (col - 1 >= 0)
                openNode(node[col - 1][row]);
            // Sur
            if (row + 1 < maxRow)
                openNode(node[col][row + 1]);
            // Este
            if (col + 1 < maxCol)
                openNode(node[col + 1][row]);
        }
    }
    
    private int getBestNodeIndex() {
        int bestNodeIndex = -1;
        int bestNodefCost = Integer.MAX_VALUE;
        
        for (int i = 0; i < openList.size(); i++) {
            Node n = openList.get(i);
            
            // Seleccionar el nodo con menor costo f
            if (n.fCost < bestNodefCost) {
                bestNodeIndex = i;
                bestNodefCost = n.fCost;
            } 
            // Si hay empate, seleccionar el que tenga menor costo g
            else if (n.fCost == bestNodefCost && n.gCost < openList.get(bestNodeIndex).gCost) {
                bestNodeIndex = i;
            }
        }
        
        return bestNodeIndex;
    }

    private void openNode(Node node) {
        // Solo abrir nodos que no estén en la lista abierta, no sean sólidos y no hayan sido revisados
        if (!node.open && !node.solid && !node.checked) {
            node.setAsOpen();
            node.parent = currentNode;
            openList.add(node);
        }
    }

    private void trackPath() {
        Node current = goalNode;
        
        // Crear el camino desde el objetivo hasta el inicio
        while (current != startNode) {
            // Agregar la ubicación del nodo actual al camino (invertido)
            path.add(new Location(current.row, current.col));
            current = current.parent;
        }
    }

    public Location getNextMove() {
        // Si no hay camino, retornar null
        if (path.isEmpty()) {
            return null;
        }
        
        // Retornar el próximo paso (último en la lista porque está invertido)
        return path.get(path.size() - 1);
    }
    
    // Clase interna para representar nodos en el algoritmo A*
    class Node {
        int row, col;
        int gCost, hCost, fCost;
        boolean start, goal, solid, open, checked;
        Node parent;

        public Node(int col, int row) {
            this.col = col;
            this.row = row;
        }

        public void setAsStart() {
            start = true;
        }

        public void setAsGoal() {
            goal = true;
        }

        public void setAsSolid() {
            solid = true;
        }
        
        public void setAsNonSolid() {
            solid = false;
        }

        public void setAsOpen() {
            open = true;
        }

        public void setAsChecked() {
            checked = true;
            open = false;
        }
    }
}
