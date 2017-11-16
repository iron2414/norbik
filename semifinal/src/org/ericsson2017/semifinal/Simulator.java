/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;

/**
 *
 * @author norbi
 */
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

    public FutureEnemy(FutureEnemy fe) {
        super(new Coord(fe.getCoord().getX(), fe.getCoord().getY()), fe.getDirX(), fe.getDirY());
        this.probability = fe.getProbability();
    }

    
    public double getProbability() {
        return probability;
    }
}

public class Simulator {
    public int[][] cells;
    public List<Enemy> enemies;
    public List<Unit> units;
    public List<List<Coord>> attackMovements;
    public List<SimResult> simulationResult;
    public List<FutureEnemy> futureEnemies;
    public int[][] futureCells;
    public List<Unit> futureUnit;
    ServerResponseParser serverResponseParser;
    
    public static final int ROWS = 80;
    public static final int COLS = 100;
    
    public static int simStep = 0;
    public List<List<FutureEnemy>> futureEnemiesHistory;    // cache

    public Simulator(ServerResponseParser serverResponseParser) {
        cells = new int[ROWS][COLS];
        futureCells = new int[ROWS][COLS];
        futureEnemies = new ArrayList<>();
        enemies = new ArrayList<>();
        units = new ArrayList<>(); 
        attackMovements = new ArrayList<>();
        simulationResult = new ArrayList<>();
        futureUnit = new ArrayList<>();
        futureEnemiesHistory = new ArrayList<>();
        this.serverResponseParser = serverResponseParser;
        
        responseChanged();
    }
    
    public final void responseChanged() {
        cells = serverResponseParser.getCells();
        enemies = serverResponseParser.getEnemies();
        units = serverResponseParser.getUnits();
    }
    
    private List<FutureEnemy> bounceEnemy(int pX, int pY, CommonClass.Direction dX, CommonClass.Direction dY, double probability)
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
    
    /**
     * Kiszámítja ez ellenégek következő lehetséges pozícióit azok valószínűségével együtt
     * és ennek megfelelően beállítja a futureEnemies listát
     */
    private void calculateEnemiesNextPos(int step)
    {
        if (futureEnemiesHistory.size() > step) {
            List<FutureEnemy> futureEnemiesClone = futureEnemiesHistory.get(step).stream().collect(Collectors.toList());
            
            futureEnemies.clear();
            futureEnemies.addAll(futureEnemiesClone);
            
            //futureEnemies.clear();
            //futureEnemies.addAll(futureEnemiesHistory.get(step));
            //System.out.println("Cached future enemy calc step, "+futureEnemies.size()+" traces.");
            return;
        }
        
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
        
        //System.out.println("New enemy traces: " + newFutureEnemies.size());
        futureEnemies.addAll(newFutureEnemies);
        
        //List<FutureEnemy> futureEnemiesClone = futureEnemies.stream().collect(Collectors.toList());
        List<FutureEnemy> futureEnemiesClone = new ArrayList<>(futureEnemies.size());
        futureEnemies.stream().map((fe) -> new FutureEnemy(fe)).forEach((futureEnemyClone) -> {
            futureEnemiesClone.add(futureEnemyClone);
        });
        futureEnemiesHistory.add(futureEnemiesClone);
    }

    private double calculateCollisionProbability()
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
    
    /**
     * Egy listában felsorolt útvonalakat rendre bejár és egy SimResult listát állít össze
     * a megnyerhető területekkel, a bejárás sikerességi valószínűségével
     * 
     * @param paths Olyan lista, melyben irányok listája és az így elfoglalható terület adatpárok vannak
     * @return
     */
    public List<SimResult> simulatePaths(List<Tuple<List<CommonClass.Direction>, Integer>> paths) {
        simulationResult.clear();        
        double totalCollProb; // az ellenséggel ütközés teljes valószínűsége %
        attackMovements.add(new ArrayList<>());
        futureEnemiesHistory.clear();

        System.out.println("\n*** SIMULATION START ***");
        
        for(int sim=0; sim<paths.size(); ++sim) {
            //System.out.println("Sim "+paths.get(sim).first.size()+" steps -- "+paths.get(sim).first.toString());
            initSim();  // minden alaphelyzetbe (támadás vektor, térkép, ellenségek, támadók)
            totalCollProb = 0.0;
            
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
            
            for(int step=0; step<paths.get(sim).first.size(); ++step) {
                CommonClass.Direction stepDir = paths.get(sim).first.get(step);
                moveUnit(futureUnit.get(0), stepDir);
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

                //System.out.println("\nStep: " + step);
                //System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos(step);
                printFutureEnemies();
                
                double collProb = calculateCollisionProbability();
                
                /*
                if (collProb > 0) {
                    System.out.println("COLLISION " + collProb + "%");
                }
                */
                
                totalCollProb = 100.0 * (totalCollProb/100.0 + ((1.0-(totalCollProb/100.0)) * (collProb/100.0)));
            }
            
            //System.out.println("\n*** Total Collision Propability: " + totalCollProb + "%\n\n");
            
            SimResult simResult = new SimResult(paths.get(sim).first.size(), 100.0-totalCollProb, paths.get(sim).second, paths.get(sim).first );
            simulationResult.add(simResult);
        }
        
        return simulationResult;
    }
    
    /*
    public List<CommonClass.Direction> findBestSteps() 
    {
        List<CommonClass.Direction> result = new ArrayList<>();
        
        double totalCollProb; // az ellenséggel ütközés teljes valószínűsége %

        attackMovements.add(new ArrayList<>());
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
                calculateEnemiesNextPos(step);
                printFutureEnemies();
            }

            // ROWS-3 lefelé
            for(; step<x+ROWS-2; step++) {
                futureUnit.get(0).coord.y++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
                
                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos(step);
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
                calculateEnemiesNextPos(step);
                printFutureEnemies();
            }

            // COLS-2 jobbra
            for(; step<y+COLS-2; step++) {
                futureUnit.get(0).coord.x++;
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
                
                System.out.println("\nStep: " + step);
                System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos(step);
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
        
        Collections.sort(simulationResult);
        printSimResult();
        
        // a 10 legjobb lehetőség közül azt választjuk, amelyiknek a valószínűsége elég nagy
        for(int i=0; i<10; ++i) {
            if (simulationResult.get(i).getSuccessProbability() >= SUCCESS_PROBABILITY_HIGH) {
                return simulationResult.get(i).getPath();
            }
        }
        
        // az első 10 között nincs elég nagy valószínűséggel megléphető
        // akkor legyel az első, mert annak a legnagyobb a valószínűsége és a területe
        return simulationResult.get(0).getPath();
    }
    */
    
    private void initSim() {
        initSim(true);
    }
    
    private void initSim(boolean clearAttackMovements) {
        // támadás vektor ürítése
        if (clearAttackMovements) attackMovements.get(0).clear();

        // jelenlegi ellenségeket átmásoljuk jövőbelieket tároló listába 100% valószínűséggel
        futureEnemies.clear();
        for(Enemy e : enemies) {
            FutureEnemy fe = new FutureEnemy(e, 100.0);
            futureEnemies.add(fe);
        }

        // jelenlegi unitokat átmásoljuk a jövőbelibe
        futureUnit.clear();
        units.stream().forEach((u) -> {
            futureUnit.add(new Unit(new Coord(u.coord.getX(), u.coord.getY()), u.health, u.killer, u.owner, u.dir));
        });

        // jelenlegi táblát átmásoljuk a jövőbelibe
        for(int xx=0; xx<ROWS; xx++) {
            System.arraycopy(cells[xx], 0, futureCells[xx], 0, COLS);
        }
    }
    
    private void printCells() {
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
    
    private void printFutureEnemies() {
        /*
        for(FutureEnemy fe : futureEnemies) {
            System.out.println("FutureEnemy: " + fe.getCoord() + " " + fe.getDirX() + " " + fe.getDirY() + " - " + fe.getProbability() + "%");
        }
        */
    }
    
    

    private void moveUnit(Unit unit, CommonClass.Direction dir) {
        switch (dir) {
            case RIGHT:
                ++unit.coord.y;
                break;
            case LEFT:
                --unit.coord.y;
                break;
            case UP:
                --unit.coord.x;
                break;
            case DOWN:
                ++unit.coord.x;
                break;
        }
    }

    /**
     * Út közbeni szimuláció: a lastStepList lépéseit már megtettük, de ezen útvonallal ütközés is "fáj"
     * azt kell megmondani, hogy a nextStepList lépéseit követve az egyes lépésekben mekkora 
     * valószínűséggel fogunk ellenséggel ütközni
     * 
     * @param lastStepList
     * @param nextStepList
     * @return 
     */
    public List<Double> simulatePathInTrip(List<CommonClass.Direction> lastStepList, List<CommonClass.Direction> nextStepList) {
        
        List<Double> collProb = new ArrayList<>(); // az ellenséggel ütközés valószínűsége a szimuláció egyes lépéseiben %
        attackMovements.add(new ArrayList<>());
        futureEnemiesHistory.clear();

        Unit unit = units.get(0);
        int uX = unit.getCoord().getX();
        int uY = unit.getCoord().getY();
        
        // a lastStepList elemit az attackMovements vektorba kell másolni úgy,
        // hogy visszafelé kell követni a lépéseket
        for(int i = lastStepList.size()-1; i>=0; --i) {
            switch (lastStepList.get(i)) {
                case UP:
                    --uX;
                    break;
                case DOWN:
                    ++uX;
                    break;
                case RIGHT:
                    ++uY;
                    break;
                case LEFT:
                    --uY;
                    break;
            }
            attackMovements.get(0).add(0, new Coord(uX, uY));   // a lista elejére kerül minden elem
        }
        
        System.out.println("\n*** SIMULATION IN TRIP START ***");
        
        initSim(false);  // minden alaphelyzetbe (támadás vektor NEM, térkép, ellenségek, támadók)

        // a támadó aktuális helyzete is kell a támadás vektorba
        attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

        for(int step=0; step<nextStepList.size(); ++step) {
            CommonClass.Direction stepDir = nextStepList.get(step);
            moveUnit(futureUnit.get(0), stepDir);
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

            //System.out.println("\nStep: " + step);
            //System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
            calculateEnemiesNextPos(step);
            printFutureEnemies();

            collProb.add(calculateCollisionProbability());
        }
        
        return collProb;
    }

    /**
     * Út közbeni többes szimuláció: a lastStepList lépéseit már megtettük, de ezen útvonallal ütközés is "fáj"
     * azt kell megmondani, hogy a nextStepLists egyes elemeiben leírt lépéseket végigkövetve
     * az ellenséges ütközés teljes valószínűsége mekkora
     * Az eredményben a valószínűségi % mellé természetesen a tesztelt lépéssor listáját is vissza kell adni
     * 
     * 
     * @param lastStepList
     * @param nextStepLists
     * @return 
     */
    public List<Tuple<Double, List<CommonClass.Direction>>> simulatePathsInTrip(List<CommonClass.Direction> lastStepList, List<List<CommonClass.Direction>> nextStepLists) {
        //List<Double> collProb = new ArrayList<>(); // az ellenséggel ütközés valószínűsége a szimuláció egyes lépéseiben %
        List<Tuple<Double, List<CommonClass.Direction>>> result = new ArrayList<>();
        double totalCollProb = 0;
        attackMovements.add(new ArrayList<>());
        futureEnemiesHistory.clear();

        Unit unit = units.get(0);
        int uX = unit.getCoord().getX();
        int uY = unit.getCoord().getY();
        
        // a lastStepList elemit az attackMovements vektorba kell másolni úgy,
        // hogy visszafelé kell követni a lépéseket
        for(int i = lastStepList.size()-1; i>=0; --i) {
            switch (lastStepList.get(i)) {
                case UP:
                    --uX;
                    break;
                case DOWN:
                    ++uX;
                    break;
                case RIGHT:
                    ++uY;
                    break;
                case LEFT:
                    --uY;
                    break;
            }
            attackMovements.get(0).add(0, new Coord(uX, uY));   // a lista elejére kerül minden elem
        }
        
        System.out.println("\n*** MULTI SIMULATION IN TRIP START ***");
        
        for(int pathStep = 0; pathStep < nextStepLists.size(); ++pathStep) {
            totalCollProb = 0;
            initSim(false);  // minden alaphelyzetbe (támadás vektor NEM, térkép, ellenségek, támadók)

            // a támadó aktuális helyzete is kell a támadás vektorba
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

            for(int step=0; step<nextStepLists.get(pathStep).size(); ++step) {
                CommonClass.Direction stepDir = nextStepLists.get(pathStep).get(step);
                moveUnit(futureUnit.get(0), stepDir);
                attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

                //System.out.println("\nStep: " + step);
                //System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
                calculateEnemiesNextPos(step);
                printFutureEnemies();

                double collProb = calculateCollisionProbability();
                
                /*
                if (collProb > 0) {
                    System.out.println("COLLISION " + collProb + "%");
                }
                */
                
                totalCollProb = 100.0 * (totalCollProb/100.0 + ((1.0-(totalCollProb/100.0)) * (collProb/100.0)));
            }
            
            result.add(new Tuple<>(totalCollProb, nextStepLists.get(pathStep)));
        }
        
        
        return result;
    }
}
