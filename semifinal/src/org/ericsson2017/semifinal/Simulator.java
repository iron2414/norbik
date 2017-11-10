/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;


import java.util.ArrayList;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;

/**
 *
 * @author norbi
 */
class Coord
{
    private int x;
    private int y;
    
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}

class Enemy 
{
    private Coord coord;
    private CommonClass.Direction dirX;
    private CommonClass.Direction dirY;
    
    public Enemy(Coord coord, CommonClass.Direction dirX, CommonClass.Direction dirY) {
        this.coord = coord;
        this.dirX = dirY;
    }
    
    public Coord getCoord() {
        return coord;
    }

    public CommonClass.Direction getDirX() {
        return dirX;
    }

    public CommonClass.Direction getDirY() {
        return dirY;
    }
}

class SimResult
{
    private int steps;
    private double successProbability;
    private int rewardArea;
    
    public SimResult(int steps, double successProbability, int rewardArea) {
        this.steps = steps;
        this.successProbability = successProbability;
        this.rewardArea = rewardArea;
    }

    public int getSteps() {
        return steps;
    }

    public double getSuccessProbability() {
        return successProbability;
    }

    public int getRewardArea() {
        return rewardArea;
    }
}

class Unit
{
    public Coord coord;
    public CommonClass.Direction dir;
    public int health;
    public int killer;
    public int owner;
    
    public Unit(Coord coord, int health, int killer, int owner) {
        this.coord = coord;
        this.health = health;
        this.killer = killer;
        this.owner = owner;
    }

    public Coord getCoord() {
        return coord;
    }

    public CommonClass.Direction getDir() {
        return dir;
    }

    public int getHealth() {
        return health;
    }

    public int getKiller() {
        return killer;
    }

    public int getOwner() {
        return owner;
    }
}

public class Simulator {
    public int[][] cells;
    public List<Enemy> enemies;    // Enemy
    public List<Unit> units;    // Unit
    public List<List<Coord>> attackMovements;    // List<Coord>
    public List<SimResult> simulationResult;   // SimResults
    
    public Simulator(ResponseClass.Response.Reader response) {
        cells = new int[80][100];
        enemies = new ArrayList<>();
        units = new ArrayList<>(); 
        attackMovements = new ArrayList<>();
        simulationResult = new ArrayList<>();
        
        // Cell init
        for(int sl=0; sl<response.getCells().size(); sl++) {
            for(int i=0; i<response.getCells().get(sl).size(); i++) {
                cells[sl][i] = response.getCells().get(sl).get(i).getOwner();
            }
        }
        
        // Enemies init
        for(int e = 0; e<response.getEnemies().size(); e++) {
            Coord coord = new Coord(response.getEnemies().get(e).getPosition().getX(), 
                    response.getEnemies().get(e).getPosition().getY());
            Enemy enemy = new Enemy(coord, response.getEnemies().get(e).getDirection().getHorizontal(), 
                    response.getEnemies().get(e).getDirection().getVertical());
            
            enemies.add(enemy);
        }
        
        // Units init
        for(int u = 0; u<response.getUnits().size(); u++) {
            Coord coord = new Coord(response.getUnits().get(0).getPosition().getX(), 
                    response.getUnits().get(0).getPosition().getY());
            Unit unit = new Unit(coord, response.getUnits().get(0).getHealth(), 
                    response.getUnits().get(0).getKiller(),
                    response.getUnits().get(0).getOwner());
            units.add(unit);
        }
    }
    
    public void printCells() {
        for(int x=0; x<80; x++) {
            for(int y=0; y<100; y++) {
                String cellValue = " ";
                
                for(Unit u : units) {
                    if (u.getCoord().getX() == x && u.getCoord().getY() == y) {
                        cellValue = "¤";
                    }
                }
                
                for(Enemy e : enemies) {
                    if (e.getCoord().getX() == x && e.getCoord().getY() == y) {
                        boolean right = e.getDirX() == CommonClass.Direction.RIGHT;
                        boolean up = e.getDirY() == CommonClass.Direction.UP;
                        
                        if (right) {
                            cellValue = up ? "↗" : "↘";
                        } else {
                            cellValue = up ? "↖" : "↙";
                        }
                    }
                }
                
                if (cellValue.equals(" ")) {
                    cellValue = Integer.toString(cells[x][y]);
                }
                
                System.out.print( cellValue );
            }
            
            System.out.println();
        }
    }
    
}
