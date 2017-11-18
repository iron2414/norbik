package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.capnproto.StructList;
import org.ericsson2017.protocol.semifinal.ResponseClass;
import static org.ericsson2017.semifinal.Simulator.COLS;
import static org.ericsson2017.semifinal.Simulator.ROWS;

/**
 *
 * @author norbi
 */
public class ServerResponseParser {
    public boolean[][] attackCans;
    public int[][] attackUnits;
    public int[][] cells;
    public List<Enemy> enemies;
	public int infoOwns;
	public int infoLevel;
	public int infoTick;
	public String status;
    public List<Unit> units;
	
	private ServerResponseParser(boolean[][] attackCans, int[][] attackUnits,
			int[][] cells, List<Enemy> enemies, int infoOwns, int infoLevel,
			int infoTick, String status, List<Unit> units) {
		this.attackCans=attackCans;
		this.attackUnits=attackUnits;
		this.cells=cells;
		this.enemies=enemies;
		this.infoOwns=infoOwns;
		this.infoLevel=infoLevel;
		this.infoTick=infoTick;
		this.status=status;
		this.units=units;
	}

    public ServerResponseParser(ResponseClass.Response.Reader response) {
        attackCans = new boolean[ROWS][COLS];
        attackUnits = new int[ROWS][COLS];
        cells = new int[ROWS][COLS];
        units = new ArrayList<>(); 
        enemies = new ArrayList<>();
        
        setResponse(response);
    }
	
	public ServerResponseParser copy() {
		return new ServerResponseParser(
				copy(attackCans),
				copy(attackUnits),
				copy(cells),
				copyEnemies(enemies),
				infoOwns,
				infoLevel,
				infoTick,
				status,
				copyUnits(units));
	}
	
	private static boolean[][] copy(boolean[][] array) {
		boolean[][] result=new boolean[array.length][];
		for (int ii=array.length-1; 0<=ii; --ii) {
			result[ii]=Arrays.copyOf(array[ii], array[ii].length);
		}
		return result;
	}
	
	private static int[][] copy(int[][] array) {
		int[][] result=new int[array.length][];
		for (int ii=array.length-1; 0<=ii; --ii) {
			result[ii]=Arrays.copyOf(array[ii], array[ii].length);
		}
		return result;
	}
	
	private static List<Enemy> copyEnemies(List<Enemy> list) {
		list=new ArrayList<>(list);
		for (int ii=list.size()-1; 0<=ii; --ii) {
			list.set(ii, list.get(ii).copy());
		}
		return list;
	}
	
	private static List<Unit> copyUnits(List<Unit> list) {
		list=new ArrayList<>(list);
		for (int ii=list.size()-1; 0<=ii; --ii) {
			list.set(ii, list.get(ii).copy());
		}
		return list;
	}
    
    public final void setResponse(ResponseClass.Response.Reader response) {
		infoLevel=response.getInfo().getLevel();
		infoOwns=response.getInfo().getOwns();
		infoTick=response.getInfo().getTick();
		status=response.getStatus().toString();
		
        // Cell init
        for(int sl=0; sl<response.getCells().size(); sl++) {
            StructList.Reader<ResponseClass.Cell.Reader> c = response.getCells().get(sl);
            for(int i=0; i<c.size(); i++) {
                attackCans[sl][i] = false;
                attackUnits[sl][i] = -1;
				switch (c.get(i).getAttack().which()) {
					case CAN:
						attackCans[sl][i] = c.get(i).getAttack().getCan();
						break;
					case UNIT:
						attackUnits[sl][i] = c.get(i).getAttack().getUnit();
						break;
				}
                cells[sl][i] = c.get(i).getOwner();
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
            Coord coord = new Coord(response.getUnits().get(u).getPosition().getX(), 
                    response.getUnits().get(u).getPosition().getY());
            Unit unit = new Unit(coord, response.getUnits().get(u).getHealth(), 
                    response.getUnits().get(u).getKiller(),
                    response.getUnits().get(u).getOwner(),
                    response.getUnits().get(u).getDirection()
            );
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
