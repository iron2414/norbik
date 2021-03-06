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
    public static final int MIN_AREA = 400; //A pálya 5%-a
    
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
        nextPosY = pY + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosX = pX + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
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
        nextPosY = pY + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosX = pX + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
        if (futureCells[nextPosX][nextPosY] == 0) {
            Coord c = new Coord(nextPosX, nextPosY);
            FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
            newEnemyList.add(fe);
        }
        
        // tud-e egyik irányban elfordulni átlósan? X irányban megy tovább, Y irányban visszafordul
        nextDX = dX;
        nextDY = dY == CommonClass.Direction.UP ? CommonClass.Direction.DOWN : CommonClass.Direction.UP;
        nextPosY = pY + (nextDX == CommonClass.Direction.RIGHT ? 1 : -1);
        nextPosX = pX + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
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
            nextPosX = pX + (nextDY == CommonClass.Direction.UP ? -1 : 1);
            nextPosY = pY;
            
            if (futureCells[nextPosX][nextPosY] == 0) {
                Coord c = new Coord(nextPosX, nextPosY);
                FutureEnemy fe = new FutureEnemy(c, nextDX, nextDY, 100.0); // a valószínűséget majd esetleg frissíteni kell
                newEnemyList.add(fe);
            }
            
            nextPosX = pX;
            nextPosX = pX + (nextDY == CommonClass.Direction.UP ? -1 : 1);
        
            
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
        
        if (futureEnemies.size() < 200) {
            for(FutureEnemy fe : futureEnemies) {        
                int posX = fe.getCoord().getX();
                int posY = fe.getCoord().getY();

                int nextPosY = posY + (fe.getDirX() == CommonClass.Direction.RIGHT ? 1 : -1);
                int nextPosX = posX + (fe.getDirY() == CommonClass.Direction.UP ? -1 : 1);

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
        }
        
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

        System.out.println("\n*** SIMULATION START ***  ("+paths.size()+" paths)");
        
        for(int sim=0; sim<paths.size(); ++sim) {
            //System.out.println("Sim "+paths.get(sim).first.size()+" steps -- "+paths.get(sim).first.toString());
            initSim();  // minden alaphelyzetbe (támadás vektor, térkép, ellenségek, támadók)
            totalCollProb = 0.0;
            
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));
            
            CommonClass.Direction stepDir = CommonClass.Direction.DOWN;
            for(int step=0; step<paths.get(sim).first.size(); ++step) {
                stepDir = paths.get(sim).first.get(step);
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
            
            // a futureEnemies elemeinek hány %-a van a vonal azonos oldalán?
            int sameSideCount = 0;
            switch (stepDir) {
                case UP:
                case DOWN:
                    int y = futureUnit.get(0).coord.y;
                    for(FutureEnemy fe : futureEnemies) {
                        sameSideCount += fe.coord.y > y ? 1 : 0;
                    }
                    break;
                case RIGHT:
                case LEFT:
                    int x = futureUnit.get(0).coord.x;
                    for(FutureEnemy fe : futureEnemies) {
                        sameSideCount += fe.coord.x > x ? 1 : 0;
                    }
                    break;
            }
            double sameSideProb = 100.0 * sameSideCount/futureEnemies.size();
            if (sameSideProb < 50.0) sameSideProb = 100.0-sameSideProb;
            
            //System.out.println("\n*** Total Collision Propability: " + totalCollProb + "%\n\n");
            
            SimResult simResult = new SimResult(paths.get(sim).first.size(), 100.0-totalCollProb, sameSideProb, paths.get(sim).second, paths.get(sim).first );
            simulationResult.add(simResult);
        }
        
        return simulationResult;
    }
    
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
        System.out.println(futureEnemies.size() + " future enemy positions | possibilities");
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
        attackMovements.clear();
        attackMovements.add(new ArrayList<>());
        futureEnemiesHistory.clear();

        Unit unit = units.get(0);
        int uX = unit.getCoord().getX();
        int uY = unit.getCoord().getY();
        
        // a lastStepList elemit az attackMovements vektorba kell másolni úgy,
        // hogy visszafelé kell követni a lépéseket ellentétes irányban
        for(int i = lastStepList.size()-1; i>=0; --i) {
            switch (lastStepList.get(i)) {
                case UP:
                    ++uX;
                    break;
                case DOWN:
                    --uX;
                    break;
                case RIGHT:
                    --uY;
                    break;
                case LEFT:
                    ++uY;
                    break;
            }
            attackMovements.get(0).add(0, new Coord(uX, uY));   // a lista elejére kerül minden elem
        }
        System.out.println("Attack movements previous steps count: "+ attackMovements.get(0).size());
        
        System.out.println("\n*** SIMULATION IN TRIP START ***  ("+nextStepList.size()+" steps)");
        
        initSim(false);  // minden alaphelyzetbe (támadás vektor NEM, térkép, ellenségek, támadók)

        // a támadó aktuális helyzete is kell a támadás vektorba
        attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

        CommonClass.Direction stepDir;
        for(int step=0; step<nextStepList.size(); ++step) {
            stepDir = nextStepList.get(step);
            moveUnit(futureUnit.get(0), stepDir);
            attackMovements.get(0).add(new Coord(futureUnit.get(0).coord.getX(), futureUnit.get(0).coord.getY()));

            //System.out.println("\nStep: " + step);
            //System.out.println("Unit: (" + futureUnit.get(0).coord.getX() + ":" + futureUnit.get(0).coord.getY() + ")");
            calculateEnemiesNextPos(step);
            printFutureEnemies();

            collProb.add(calculateCollisionProbability());
        }
        //TODO mintha az attackMovements nem mindig lenne jó
        System.out.println("Attack movements count: "+ attackMovements.get(0).size());
        System.out.println("Collision probabilities:");
        for(double prob : collProb) {
            System.out.print(prob+", ");
        }
        System.out.println("");
        
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
        
        attackMovements.clear();
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
                    ++uX;
                    break;
                case DOWN:
                    --uX;
                    break;
                case RIGHT:
                    --uY;
                    break;
                case LEFT:
                    ++uY;
                    break;
            }
            attackMovements.get(0).add(0, new Coord(uX, uY));   // a lista elejére kerül minden elem
        }
        
        System.out.println("\n*** MULTI SIMULATION IN TRIP START ***");
        
        double totalCollProb;
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
            List<Coord> temp = new ArrayList<>(attackMovements.get(0).subList(0, lastStepList.size()));
            attackMovements.get(0).clear();
            attackMovements.get(0).addAll(temp);
        }
        
        
        return result;
    }
}
