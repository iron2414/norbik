package org.ericsson2017.semifinal;

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
     * @return Tuple Az irány lista és az áthaladás valószínűsége
     * @throws java.lang.Throwable
     */
    public Tuple<List<CommonClass.Direction>, Double> findPath() throws Throwable {
        // véletlenül a harcmezőn ragadtunk [biztonsági feature]
        if (pathFinder.inEmptyField()) {
            // megyünk tovább
            return new Tuple<>(pathFinder.findUnitLastDirection(), 100.0);
        }
        // harcmező szélén állunk?
        if (pathFinder.nearEmptyField()) {
            List<Tuple<List<CommonClass.Direction>, Integer>> pathAndAreas = pathFinder.findCrossPaths();
            List<SimResult> simResult = simulator.simulatePaths(pathAndAreas);
            printSimResult(simResult);
            return optiPathSel.findOptimalPath(simResult);
        } else {
            return new Tuple<>(pathFinder.findShortestPathToEmptyField(), 100.0);
        }
    }
    
    private void printSimResult(List<SimResult> simulationResult) {
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
