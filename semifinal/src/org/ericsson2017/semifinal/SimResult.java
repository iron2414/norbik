package org.ericsson2017.semifinal;

import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;

/**
 *
 * @author norbi
 */
public class SimResult implements Comparable<SimResult>
{
    private final int steps;
    private final double successProbability;
    private final double allEnemiesInSameSideProbability;
    private final int rewardArea;
    private final List<CommonClass.Direction> path;
    
    public SimResult(
            int steps, 
            double successProbability, 
            double allEnemiesInSameSideProbability, 
            int rewardArea, 
            List<CommonClass.Direction> path
    ) {
        this.steps = steps;
        this.successProbability = successProbability;
        this.allEnemiesInSameSideProbability = allEnemiesInSameSideProbability;
        this.rewardArea = rewardArea;
        this.path = path;
    }

    @Override
    public int compareTo(SimResult r) {
        if ((allEnemiesInSameSideProbability>80.0 && r.allEnemiesInSameSideProbability>80.0) || allEnemiesInSameSideProbability==r.allEnemiesInSameSideProbability) {
            if (rewardArea == r.rewardArea) {
                if (successProbability == r.successProbability) {
                    return steps > r.steps ? -1 : (steps == r.steps ? 0 : 1);
                } else {
                    return successProbability > r.successProbability ? -1 : 1;
                }
            } else {
                return rewardArea > r.rewardArea ? -1 : 1;
            }
        } else {
           return allEnemiesInSameSideProbability > r.allEnemiesInSameSideProbability ? -1 : 1;
        }
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

    public double getAllEnemiesInSameSideProbability() {
        return allEnemiesInSameSideProbability;
    }
    
}