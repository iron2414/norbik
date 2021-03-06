package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;

/**
 *
 * @author norbi
 */
public class SimManager {
    
    ServerResponseParser serverResponseParser;
    PathFinder pathFinder;
    Simulator simulator;
    OptimalPathSelector optiPathSel;

    public SimManager(ResponseClass.Response.Reader response) {
        serverResponseParser = new ServerResponseParser(response);
        pathFinder = new PathFinder(serverResponseParser);
        simulator = new Simulator(serverResponseParser);
        optiPathSel = new OptimalPathSelector();
    }
    
    public void setResponse(ResponseClass.Response.Reader response) {
        serverResponseParser.setResponse(response);
        pathFinder.responseChanged();
        simulator.responseChanged();
    }
    
    /**
     * Megkeressi azt a legjobb útvonalat, mellyel az aktuális pozícióból a legrövidebb úton
     * a legnagyobb területnyereséget lehet elérni a legnagyobb valószínűséggel
     * Ha nem a harcmező szélén állunk, akkor a legrövidebb úton előbb az oda vezető utat adja vissza!!!
     * 
     * @param winArea
     * @param wonArea
     * @return Tuple Az irány lista és az áthaladás valószínűsége
     * @throws java.lang.Throwable
     */
    public Tuple<List<CommonClass.Direction>, Double> findPath(boolean winArea) throws Throwable {
        // véletlenül a harcmezőn ragadtunk [biztonsági feature]
        if (pathFinder.inEmptyField()) {
            // megyünk tovább
            return new Tuple<>(pathFinder.findUnitLastDirection(), 100.0);
        } 
        //Az új még nem kész verzió
        
        else {
            Tuple<Coord, Coord> rectangle = pathFinder.getBiggestRectangle();
            System.out.println("TopLeft: " + rectangle.first.x + ":" + rectangle.first.y);
            System.out.println("BottomRight: " + rectangle.second.x + ":" + rectangle.second.y);
        
            if (pathFinder.isUnitInRect(rectangle)) {
                Tuple<Coord, Coord> perpendicularWalls = pathFinder.getNearestPerpendicularWalls(rectangle);
                System.out.println("Horizontal: " + perpendicularWalls.first.x + ":" + perpendicularWalls.first.y);
                System.out.println("Vertical: " + perpendicularWalls.second.x + ":" + perpendicularWalls.second.y);
                
                // TODO: pathFinder-rel U alakú útvonalakat terveztetni unit0 és a merőleges falak "közé"
                List<Tuple<List<CommonClass.Direction>, Integer>> pathAndAreas = pathFinder.generateUPaths(perpendicularWalls, rectangle);
                // TODO: leszimuláltatni az előállt útvonal-listát
                // TODO: kiválasztani a legjobbat és visszatérni vele
                List<SimResult> simResult = simulator.simulatePaths(pathAndAreas);
                    //printSimResult(simResult);
                return optiPathSel.findOptimalPath(simResult);
            } else {
                // a régi kód
                if (pathFinder.nearEmptyField()) {
                    List<Tuple<List<CommonClass.Direction>, Integer>> pathAndAreas = pathFinder.findCrossPaths();
                    List<SimResult> simResult = simulator.simulatePaths(pathAndAreas);
                    //printSimResult(simResult);
                    return optiPathSel.findOptimalPath(simResult);
                } else {
                    return new Tuple<>(pathFinder.findShortestPathToEmptyField(), 100.0);
                }
            }
        }
        
        // harcmező szélén állunk?
        //Az új specifikáció miatt ezek kikerültek a felette lévő else került be.
        /*
        if (pathFinder.nearEmptyField() && winArea) {
            List<Tuple<List<CommonClass.Direction>, Integer>> pathAndAreas = pathFinder.findCrossPaths();
            List<SimResult> simResult = simulator.simulatePaths(pathAndAreas);
            //printSimResult(simResult);
            return optiPathSel.findOptimalPath(simResult);
        } else {
            return new Tuple<>(pathFinder.findShortestPathToEmptyField(), 100.0);
        }
        */
    }
    
    /**
     * Ellenőrizze le, hogy a megadott útvonal hátralévő részét végigjárva, az egyes lépések során
     * mekkora valószínűséggel fogunk ellenséggel ütközni, és ha ez elég nagy, akkor
     * keressen menekülő útvonalat és módosítsa úgy az útvonalat, hogy ezzel elkrüljük (legalább egy időre)
     * az ütközést. 
     * Az eredmény az eddig megtett lépéseket és az ez utáni megcélzott lépéseket is tartalmazza.
     * A "currentStep" indexet már megléptem.
     * 
     * @param stepList
     * @param currentStep   Ezt a lépést már megtettük
     * @return 
     */
    public List<CommonClass.Direction> checkPath(List<CommonClass.Direction> stepList, int currentStep) 
    {
        List<CommonClass.Direction> result = new ArrayList<>(stepList.size());
        List<CommonClass.Direction> remainigSteps = new ArrayList<>();
        
        // ha már az utolsó lépésnél vagyunk, ne változtassunk
        if (stepList.size() <= currentStep+1) {
            result.addAll(stepList);
            return result;
        }
        
        // Felül nyitott, alul zárt intervalumot kell adni a subList-nek!!!
        result.addAll(stepList.subList(0, currentStep+1));    // az eddigi lépések maradnak, a szimulációhoz kellenek
        remainigSteps.addAll(stepList.subList(currentStep+1, stepList.size())); // a tervezett további lépések
        
        // a _hátralévő_ lépésekben mekkora az ütközés valószínűsége?
        List<Double> collProbList = simulator.simulatePathInTrip(result, remainigSteps); 
        
        // ha bármelyik lépésben kétesélyes, hogy ütközni fogok-e, akkor 50% vagy annál kisebb a valószínűség
        // ha 50%-nál nagyobb értéket találok, akkor szinte biztos az ütközés, menekülni kell!
        boolean findEscapePath = false;
        int collisionStep = 0;
        for (int i=0; i<collProbList.size(); ++i) {
            //if (collProbList.get(i) > 50.0 || collProbList.get(i) == 50 && result.size() > i ) {
            if (collProbList.get(i) >= 50.0) {
                findEscapePath = true;
                collisionStep = i;
                break;
            }
        }
        
        // nem lesz ütközés
        if (!findEscapePath) {
            result.addAll(remainigSteps);
        } else {
            // valahol a jövőben ütközés lesz, de csak akkor kell menekülni,
            // ha a rövidebbik menekülőút megtétele után még pont nem ütközünk
            // ehhez szükség van a menekülő utakra
            List<List<CommonClass.Direction>> paths = pathFinder.findEscapeRoutes(stepList.get(currentStep+1));
            int shortestEscapeRoute = Integer.MAX_VALUE;
            for(List<CommonClass.Direction> path : paths) {
                shortestEscapeRoute = Math.min(shortestEscapeRoute, path.size());
            }
            
            System.out.println(String.format("%1$d steps to collision, but the shortest escape route needs %2$d steps.", collisionStep, shortestEscapeRoute));
            if (collisionStep <= shortestEscapeRoute) {
                // tuti az ütközés, de menekülni, mert a menekülőút biztonságosabb
                System.out.println("*** Collision probability too high, find escape route!");

                List<Tuple<Double, List<CommonClass.Direction>>> collProbsList = simulator.simulatePathsInTrip(result, paths);
                printCollProbsList(collProbsList);
                result.addAll(optiPathSel.findOptimalEscapePath(collProbsList));
            } else {
                result.addAll(remainigSteps);
            }
        }
        return result;
    }
    
    private void printSimResult(List<SimResult> simulationResult) {
        System.out.println("*** Simulation result before ordering ***");
        System.out.println("Steps | Area | SuccessRate | EnemiesInSameSide% | Path");
        
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
            System.out.println(sr.getSteps() + " | " + sr.getRewardArea() + " | " + sr.getSuccessProbability()+"% | " + sr.getAllEnemiesInSameSideProbability() + "% | " + p);
        }
    }

    private void printCollProbsList(List<Tuple<Double, List<CommonClass.Direction>>> collProbsList) {
        System.out.println("*** Escape Path Simulation result ***");
        System.out.println("SuccessRate | Path");
        
        for(Tuple<Double, List<CommonClass.Direction>> collProbs : collProbsList) {
            String p = "";
            for(int i=0; i<collProbs.second.size(); i++) {
                switch(collProbs.second.get(i)) {
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
            System.out.println(collProbs.first + "% | " + p);
        }
    }

    /**
     * Az utolsó mozgásirány ismeretében nyertünk-e területet?
     * Azaz visszanézve az átlós irányokban van-e saját terület?
     * 
     * @param lastDir
     * @return 
     */
    boolean hasWinArea(CommonClass.Direction lastDir) {
        
        return pathFinder.hasWinArea(lastDir);
    }
    
    public CommonClass.Direction checkMove(CommonClass.Direction dir) {
        return pathFinder.checkMove(dir);
    }
}
