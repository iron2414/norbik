package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.List;
import org.ericsson2017.protocol.semifinal.ResponseClass;
import static org.ericsson2017.semifinal.Simulator.COLS;
import static org.ericsson2017.semifinal.Simulator.ROWS;

/**
 *
 * @author norbi
 */
public class ServerResponseParser {
    public int[][] cells;
    public List<Unit> units;
    public List<Enemy> enemies;

    public ServerResponseParser(ResponseClass.Response.Reader response) {
        cells = new int[ROWS][COLS];
        units = new ArrayList<>(); 
        enemies = new ArrayList<>();
        
        setResponse(response);
    }
    
    public final void setResponse(ResponseClass.Response.Reader response) {
        // Cell init
        for(int sl=0; sl<response.getCells().size(); sl++) {
            for(int i=0; i<response.getCells().get(sl).size(); i++) {
                cells[sl][i] = response.getCells().get(sl).get(i).getOwner();
            }
        }
        
        // Enemies init
        enemies.clear();
        for(int e = 0; e<response.getEnemies().size(); e++) {
            Coord coord = new Coord(response.getEnemies().get(e).getPosition().getX(), 
                    response.getEnemies().get(e).getPosition().getY());
            Enemy enemy = new Enemy(coord, response.getEnemies().get(e).getDirection().getHorizontal(), 
                    response.getEnemies().get(e).getDirection().getVertical());
            
            enemies.add(enemy);
        }
        
        // Units init
        units.clear();
        for(int u = 0; u<response.getUnits().size(); u++) {
            Coord coord = new Coord(response.getUnits().get(0).getPosition().getX(), 
                    response.getUnits().get(0).getPosition().getY());
            Unit unit = new Unit(coord, response.getUnits().get(0).getHealth(), 
                    response.getUnits().get(0).getKiller(),
                    response.getUnits().get(0).getOwner());
            units.add(unit);
        }
    }
    
    public int[][] getCells() {
        return cells;
    }
    
    public List<Unit> getUnits() {
        return units;
    }
    
    public List<Enemy> getEnemies() {
        return enemies;
    }

}
