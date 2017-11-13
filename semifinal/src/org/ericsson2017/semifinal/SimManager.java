package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import org.ericsson2017.protocol.semifinal.ResponseClass;
import static org.ericsson2017.semifinal.Simulator.COLS;
import static org.ericsson2017.semifinal.Simulator.ROWS;

/**
 *
 * @author norbi
 */
public class SimManager {
    
    PathFinder pathFinder;
    Simulator simulator;
    OptimalPathSelector opiPathSel;

    public SimManager(ResponseClass.Response.Reader response) {
        pathFinder = new PathFinder(response);
        simulator = new Simulator(response);
        opiPathSel = new OptimalPathSelector();
    }
    
    /**
     * Megkeressi azt a legjobb útvonalat, mellyel az aktuális pozícióból a legrövidebb úton
     * a legnagyobb területnyereséget lehet elérni a legnagyobb valószínűséggel
     * Ha nem a harcmező szélén állunk, akkor a legrövidebb úton előbb az oda vezető utat adja vissza!!!
     * 
     * @return Tuple Az irány lista és az áthaladás valószínűsége
     */
    public Tuple<List<CommonClass.Direction>, Double> findPath() {
        // harcmező szélén állunk?
        if (pathFinder.nearEmptyField()) {    
            List<Tuple<List<CommonClass.Direction>, Integer>> pathAndAreas = pathFinder.findCrossPaths();
            List<SimResult> simResult = simulator.simulatePaths(pathAndAreas);
            return opiPathSel.findOptimalPath(simResult);
        } else {
            return new Tuple<>(pathFinder.findShortestPathToEmptyField(), 100.0);
        }
    }
    
}
