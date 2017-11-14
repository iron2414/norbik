/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;

import java.util.Collections;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;

/**
 *
 * @author norbi
 */
public class OptimalPathSelector {
    
    /**
     * "Elég nagy" sikerességi ráta -> ha ennél nagyobb egy áthaladás sikerességének
     * valószínűsége, akkor érdemes megpróbálni
     */
    public static final double SUCCESS_PROBABILITY_HIGH = 95.0;
    /**
     * "Elég nagy" területnyereség -> a maximálisan megszerezhető terület ennyi %-a már jó kompromisszum
     */
    public static final double REWARD_AREA_BIG_ENOUGH = 60.0;
    
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
        
        Tuple<List<CommonClass.Direction>, Double> bestResult = null;
        int areaOfBestResult = 0;
        
        // az első olyan, amelyiknek a valószínűsége elég nagy
        for(SimResult sr : simulationResult) {
            if (sr.getSuccessProbability() >= SUCCESS_PROBABILITY_HIGH) {
                bestResult = new Tuple(sr.getPath(), sr.getSuccessProbability());
                areaOfBestResult = sr.getRewardArea();
            }
        }
        
        if (bestResult != null) {
            // a területszerzés is elég nagy?
            int maxArea = simulationResult.get(0).getRewardArea();
            if (areaOfBestResult > maxArea * REWARD_AREA_BIG_ENOUGH/100.0) {
                return bestResult;
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
