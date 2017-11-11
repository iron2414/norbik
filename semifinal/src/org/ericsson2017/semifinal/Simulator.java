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
    public int x;
    public int y;
    
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
    
    @Override
    public String toString() {
        return "(" + x + ":" + y + ")";
    }
}

class Enemy 
{
    protected Coord coord;
    protected CommonClass.Direction dirX;
    protected CommonClass.Direction dirY;
    
    public Enemy(Coord coord, CommonClass.Direction dirX, CommonClass.Direction dirY) {
        this.coord = coord;
        this.dirX = dirX;
        this.dirY = dirY;
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

class FutureEnemy extends Enemy 
{
    public double probability;
            
    public FutureEnemy(Coord coord, CommonClass.Direction dirX, CommonClass.Direction dirY, double probability) {
        super(coord, dirX, dirY);
        this.probability = probability;
    }
    
    public FutureEnemy(Enemy enemy, double probability) {
        super(new Coord(enemy.getCoord().getX(), enemy.getCoord().getY()), enemy.getDirX(), enemy.getDirY());
        this.probability = probability;
    }

    
    public double getProbability() {
        return probability;
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
    public List<FutureEnemy> futureEnemies = new ArrayList<>();
    public int[][] futureCells;
    
    public static final int ROWS = 80;
    public static final int COLS = 100;
    
    public Simulator(ResponseClass.Response.Reader response) {
        cells = new int[ROWS][COLS];
        futureCells = new int[ROWS][COLS];
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
        for(int x=0; x<ROWS; x++) {
            for(int y=0; y<COLS; y++) {
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
    
    public void bounceEnemy(List<FutureEnemy> futureEnemyList, int pX, int pY, CommonClass.Direction dX, CommonClass.Direction dY, double probability)
    {
        int nextPosX;
        int nextPosY;
        CommonClass.Direction nextDX;
        CommonClass.Direction nextDY;
        boolean canRetreat = true;
        List<FutureEnemy> newEnemyList = new ArrayList<>();
        
        // vissza tud pattanni?
        nextDX = dX == CommonClass.Direction.RIGHT ? CommonClass.Direction.LEFT : CommonClass.Direction.RIGHT;
        nextDY = dY == CommonClass.Direction.UP ? CommonClass.Direction.DOWN : CommonClass.Direction.UP;
        nextPosX = pX + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosY = pY + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
        if (futureCells[nextPosX][nextPosY] == 0) {
            Coord c = new Coord(nextPosX, nextPosY);
            FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
            newEnemyList.add(fe);
        } else {
            canRetreat = false;
        }
        
        // tud-e egyik irányban elfordulni átlósan? X irányban visszafordul, Y irányban megy tovább
        nextDX = dX == CommonClass.Direction.RIGHT ? CommonClass.Direction.LEFT : CommonClass.Direction.RIGHT;
        nextDY = dY;
        nextPosX = pX + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosY = pY + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
        if (futureCells[nextPosX][nextPosY] == 0) {
            Coord c = new Coord(nextPosX, nextPosY);
            FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
            newEnemyList.add(fe);
        }
        
        // tud-e egyik irányban elfordulni átlósan? X irányban megy tovább, Y irányban visszafordul
        nextDX = dX;
        nextDY = dY == CommonClass.Direction.UP ? CommonClass.Direction.DOWN : CommonClass.Direction.UP;
        nextPosX = pX + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosY = pY + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
        if (futureCells[nextPosX][nextPosY] == 0) {
            Coord c = new Coord(nextPosX, nextPosY);
            FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
            newEnemyList.add(fe);
        }
        
        if (newEnemyList.size() == 0) {
            // semerre sem tud szabályosan pattanni -> vízszintesen vagy függőlegesen is mozoghat, 
            // de az irányvektora visszafelé kell hogy mutasson
            nextDX = dX == CommonClass.Direction.RIGHT ? CommonClass.Direction.LEFT : CommonClass.Direction.RIGHT;
            nextDY = dY == CommonClass.Direction.UP ? CommonClass.Direction.DOWN : CommonClass.Direction.UP;
            nextPosX = pX + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
            nextPosY = pY;
            
            if (futureCells[nextPosX][nextPosY] == 0) {
                Coord c = new Coord(nextPosX, nextPosY);
                FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
                newEnemyList.add(fe);
            }
            
            nextPosX = pX;
            nextPosY = pY + (nextDY == CommonClass.Direction.UP ? -1 : 1);
            
            if (futureCells[nextPosX][nextPosY] == 0) {
                Coord c = new Coord(nextPosX, nextPosY);
                FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
                newEnemyList.add(fe);
            }
        }
        
        for(FutureEnemy ne : newEnemyList) {
            ne.probability = probability / newEnemyList.size();
        }
    }
    
    public void calculateEnemiesNextPos()
    {
        List<FutureEnemy> newFutureEnemies = new ArrayList<>();
        
        for(FutureEnemy fe : futureEnemies) {
            int posX = fe.getCoord().getX();
            int posY = fe.getCoord().getY();
            
            int nextPosX = posX + (fe.getDirX() == CommonClass.Direction.RIGHT ? 1 : -1);
            int nextPosY = posY + (fe.getDirY() == CommonClass.Direction.UP ? -1 : 1);
            
            if (futureCells[nextPosX][nextPosY] > 0) {
                // pattanni kell
                List<FutureEnemy> bouncedEnemies = new ArrayList<>();
                
                // a bouncedEnemies listában benne lesz az összes pattanás az új valószínűségekkel
                bounceEnemy(bouncedEnemies, posX, posY, fe.getDirX(), fe.getDirY(), fe.getProbability());
                
                // az aktuális ellenség irányát és helyzetét frissítjük a lista első elemével
                fe.coord = new Coord(bouncedEnemies.get(0).coord.getX(), bouncedEnemies.get(0).coord.getY());
                fe.dirX = bouncedEnemies.get(0).getDirX();
                fe.dirY = bouncedEnemies.get(0).getDirY();
                fe.probability = bouncedEnemies.get(0).getProbability();
                
                // ha van a listában más is, akkor abból új ellenségeket hozunk létre
                if (bouncedEnemies.size() > 1) {
                    for(int i=1; i<bouncedEnemies.size(); i++) {
                        FutureEnemy f = new FutureEnemy(
                                new Coord(bouncedEnemies.get(i).coord.getX(), bouncedEnemies.get(i).coord.getY()), 
                                bouncedEnemies.get(i).dirX,
                                bouncedEnemies.get(i).dirY,
                                bouncedEnemies.get(i).getProbability()
                        );
                        newFutureEnemies.add(f);
                    }
                }
                
                // nincs már szükség a listára
                bouncedEnemies.clear();
            } else {
                // az adott irányban szabad a pálya, megyünk tovább
                fe.getCoord().x = nextPosX;
                fe.getCoord().y = nextPosY;
            }
        }
        
        System.out.println("New enemy traces: " + newFutureEnemies.size());
        futureEnemies.addAll(newFutureEnemies);
    }

    
    public List<CommonClass.Direction> findBestSteps() 
    {
        List<CommonClass.Direction> result = new ArrayList<>();
        List<Unit> futureUnit = new ArrayList<>();
        
        // előbb jobbra megyünk valamennyit, aztán lefele végig
        // 3-nél kevesebbet nem érdemes jobbra menni
        for(int x=3; x<COLS-3; x++) {
            // jelenlegi ellenségeket átmásoljuk jövőbelieket tároló listába 100% valószínűséggel
//System.out.println(enemies.get(0).getCoord() + " " + enemies.get(0).getDirX() + " " + enemies.get(0).getDirY());
            futureEnemies.clear();
            //enemies.stream().map((e) -> new FutureEnemy(e, 100.0)).forEach((e) -> futureEnemies.add(e));
            for(Enemy e : enemies) {
                FutureEnemy fe = new FutureEnemy(e, 100.0);
                futureEnemies.add(fe);
            }
            
            // jelenlegi unitokat átmásoljuk a jövőbelibe
            futureUnit.clear();
            units.stream().forEach((u) -> {
                futureUnit.add(u);
            });
            
            // jelenlegi táblát átmásoljuk a jövőbelibe
            for(int xx=0; xx<ROWS; xx++) {
                System.arraycopy(cells[xx], 0, futureCells[xx], 0, COLS);
            }   
            
            // x lépés jobbra
            for(int step=0; step<x; step++) {
                futureUnit.get(0).coord.x++;
                calculateEnemiesNextPos();
                
                System.out.println("Step: " + step);
                printFutureEnemies();
            }
        }
        
        // előbb lefele megyünk valamennyit, aztén jobbra
        
        
        return result;
    }
    
    public void printFutureEnemies() {
        for(FutureEnemy fe : futureEnemies) {
            System.out.println("FE: " + fe.getCoord() + " " + fe.getDirX() + " " + fe.getDirY() + " - " + fe.getProbability());
        }
    }
}
