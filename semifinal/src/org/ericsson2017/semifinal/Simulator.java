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
    private List<CommonClass.Direction> path;
    
    public SimResult(int steps, double successProbability, int rewardArea, List<CommonClass.Direction> path) {
        this.steps = steps;
        this.successProbability = successProbability;
        this.rewardArea = rewardArea;
        this.path = path;
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

    public List<CommonClass.Direction> getPath() {
        return path;
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
    public List<Unit> futureUnit;
    
    public static final int ROWS = 80;
    public static final int COLS = 100;
    
    public Simulator(ResponseClass.Response.Reader response) {
        cells = new int[ROWS][COLS];
        futureCells = new int[ROWS][COLS];
        enemies = new ArrayList<>();
        units = new ArrayList<>(); 
        attackMovements = new ArrayList<>();
        simulationResult = new ArrayList<>();
        futureUnit = new ArrayList<>();
        
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
    
    public List<FutureEnemy> bounceEnemy(int pX, int pY, CommonClass.Direction dX, CommonClass.Direction dY, double probability)
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
        
        return newEnemyList;
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
                // a bouncedEnemies listában benne lesz az összes pattanás az új valószínűségekkel
                List<FutureEnemy> bouncedEnemies = bounceEnemy(posX, posY, fe.getDirX(), fe.getDirY(), fe.getProbability());
assert bouncedEnemies.size() > 0; // "Enemy cannot bounce"
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
            } else {
                // az adott irányban szabad a pálya, megyünk tovább
                fe.getCoord().x = nextPosX;
                fe.getCoord().y = nextPosY;
            }
        }
        
        System.out.println("New enemy traces: " + newFutureEnemies.size());
        futureEnemies.addAll(newFutureEnemies);
    }

    public double calculateCollisionProbability()
    {
        // TODO: csak 1 támadóra számol jelenleg
        double result = 0;
        for(int a=0; a<attackMovements.get(0).size(); a++) {
            for(int e=0; e<futureEnemies.size(); e++) {
                if (attackMovements.get(0).get(a).getX() == futureEnemies.get(e).coord.getX() &&
                    attackMovements.get(0).get(a).getY() == futureEnemies.get(e).coord.getY()) {
                    //System.out.println("Collision with ("+futureEnemies.get(e).coord.getX()+":"+futureEnemies.get(e).coord.getY()+")");
                    result += futureEnemies.get(e).getProbability();
                }
            }
        }
        
        return Math.min(result, 100.0);
    }
    
    public List<CommonClass.Direction> findBestSteps() 
    {
        List<CommonClass.Direction> result = new ArrayList<>();
        
        double totalCollProb = 0.0; // az ellenséggel ütközés teljes valószínűsége %

        attackMovements.add(new ArrayList<Coord>());
        simulationResult.clear();
        
        // előbb jobbra megyünk valamennyit, aztán lefele végig
        // 3-nél kevesebbet nem érdemes jobbra menni
        for(int x=3; x<COLS-3; x++) {
            
            // *** SZIMULÁCIÓ INDUL ***
            initSim();  // minden alaphelyzetbe (támadás vektor, térkép, ellenségek, támadók)
            totalCollProb = 0.0;
            
            // x lépés jobbra
            int step;
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
            for(step=0; step<x; step++) {
                futureUnit.get(0).coord.x++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos();
                printFutureEnemies();
            }

            // ROWS-3 lefelé
            for(; step<x+ROWS-2; step++) {
                futureUnit.get(0).coord.y++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
                
                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos();
                printFutureEnemies();
                double collProb = calculateCollisionProbability();
                
                if (collProb > 0) {
                    System.out.println("COLLISION " + collProb + "%");
                }
                
                totalCollProb = 100.0 * (totalCollProb/100.0 + ((1.0-(totalCollProb/100.0)) * (collProb/100.0)));
            }
            
            System.out.println("\n*** Total Collision Propability: " + totalCollProb + "%\n\n");
            
            List<CommonClass.Direction> path =new ArrayList<>();
            for(int i=0; i<attackMovements.get(0).size()-1; i++) {
                int dX = attackMovements.get(0).get(i+1).x - attackMovements.get(0).get(i).x;
                int dY = attackMovements.get(0).get(i+1).y - attackMovements.get(0).get(i).y;
                CommonClass.Direction dir = (dX == 0 ? (dY > 0 ? CommonClass.Direction.DOWN : CommonClass.Direction.UP) : (dX > 0 ? CommonClass.Direction.RIGHT : CommonClass.Direction.LEFT) );
                path.add(dir);
            }
            SimResult simResult = new SimResult(step, 100.0-totalCollProb, Math.min((x-1) * (ROWS-4), (COLS-2-x) * (ROWS-4)), path );
            simulationResult.add(simResult);
        }
        
        
        // előbb lefele megyünk valamennyit, aztán jobbra
        // 2-nél kevesebbet nem érdemes lefele menni
        for(int y=2; y<ROWS-4; y++) {
            
            // *** SZIMULÁCIÓ INDUL ***
            initSim();  // minden alaphelyzetbe (támadás vektor, térkép, ellenségek, támadók)
            totalCollProb = 0.0;
            
            // y lépés lefele
            int step;
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
            for(step=0; step<y; step++) {
                futureUnit.get(0).coord.y++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos();
                printFutureEnemies();
            }

            // COLS-2 jobbra
            for(; step<y+COLS-2; step++) {
                futureUnit.get(0).coord.x++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
                
                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos();
                printFutureEnemies();
                double collProb = calculateCollisionProbability();
                
                if (collProb > 0) {
                    System.out.println("COLLISION " + collProb + "%");
                }
                
                totalCollProb = 100.0 * (totalCollProb/100.0 + ((1.0-(totalCollProb/100.0)) * (collProb/100.0)));
            }
            
            System.out.println("\n*** Total Collision Propability: " + totalCollProb + "%\n\n");
            
            List<CommonClass.Direction> path =new ArrayList<>();
            for(int i=0; i<attackMovements.get(0).size()-1; i++) {
                int dX = attackMovements.get(0).get(i+1).x - attackMovements.get(0).get(i).x;
                int dY = attackMovements.get(0).get(i+1).y - attackMovements.get(0).get(i).y;
                CommonClass.Direction dir = (dX == 0 ? (dY > 0 ? CommonClass.Direction.DOWN : CommonClass.Direction.UP) : (dX > 0 ? CommonClass.Direction.RIGHT : CommonClass.Direction.LEFT) );
                path.add(dir);
            }
            SimResult simResult = new SimResult(step, 100.0-totalCollProb, Math.min(y*(COLS-4), (ROWS-3-y) * (COLS-4)), path );
            simulationResult.add(simResult);
        }
        
        
        printSimResult();
        return result;
    }
    
    private void initSim() {
        // támadás vektor ürítése
        attackMovements.get(0).clear();

        // jelenlegi ellenségeket átmásoljuk jövőbelieket tároló listába 100% valószínűséggel
        futureEnemies.clear();
        for(Enemy e : enemies) {
            FutureEnemy fe = new FutureEnemy(e, 100.0);
            futureEnemies.add(fe);
        }

        // jelenlegi unitokat átmásoljuk a jövőbelibe
        futureUnit.clear();
        units.stream().forEach((u) -> {
            futureUnit.add(new Unit(new Coord(u.coord.getX(), u.coord.getY()), u.health, u.killer, u.owner));
        });

        // jelenlegi táblát átmásoljuk a jövőbelibe
        for(int xx=0; xx<ROWS; xx++) {
            System.arraycopy(cells[xx], 0, futureCells[xx], 0, COLS);
        }
    }
    
    public void printFutureEnemies() {
        for(FutureEnemy fe : futureEnemies) {
            System.out.println("FutureEnemy: " + fe.getCoord() + " " + fe.getDirX() + " " + fe.getDirY() + " - " + fe.getProbability() + "%");
        }
    }
    
    public void printSimResult() {
        System.out.println("*** Simulation result ***");
        System.out.println("Steps | Area | SuccessRate | Path");
        
        for(SimResult sr : simulationResult) {
            String p = "";
            for(int i=0; i<sr.getPath().size(); i++) {
                switch(sr.getPath().get(i)) {
                    case DOWN:
                        p += "D";
                        break;
                    case UP:
                        p += "U";
                        break;
                    case RIGHT:
                        p += "R";
                        break;
                    case LEFT:
                        p += "L";
                        break;
                }
            }
            System.out.println(sr.getSteps() + " | " + sr.getRewardArea() + " | " + sr.getSuccessProbability()+"% | " + p);
        }
    }
}
