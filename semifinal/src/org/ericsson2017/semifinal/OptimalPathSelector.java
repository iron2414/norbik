/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;

import java.util.Collections;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import static org.ericsson2017.semifinal.Simulator.SUCCESS_PROBABILITY_HIGH;

/**
 *
 * @author norbi
 */
public class OptimalPathSelector {
    
    /**
     * Optimális áthaladási útvonalat keres a szimuláció eredménye alapján
     * 
     * TODO: biztosan lehet ennél ügyesebben is választani!!!!
     * 
     * @param simulationResult
     * @return 
     */
    public Tuple<List<CommonClass.Direction>, Double> findOptimalPath(List<SimResult> simulationResult) {
        
        Collections.sort(simulationResult);
        printSimResult(simulationResult);
        
        // a 10 legjobb lehetőség közül azt választjuk, amelyiknek a valószínűsége elég nagy
        for(int i=0; i<10; ++i) {
            if (simulationResult.get(i).getSuccessProbability() >= SUCCESS_PROBABILITY_HIGH) {
                return new Tuple(simulationResult.get(i).getPath(), simulationResult.get(i).getSuccessProbability());
            }
        }
        
        // az első 10 között nincs elég nagy valószínűséggel megléphető
        // akkor legyel az első, mert annak a legnagyobb a valószínűsége és a területe
        return new Tuple(simulationResult.get(0).getPath(), simulationResult.get(0).getSuccessProbability());
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
