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
public class PathFinder {
    public int[][] cells;
    public List<Unit> units;
    
    public PathFinder(ResponseClass.Response.Reader response) {
        cells = new int[ROWS][COLS];
        units = new ArrayList<>(); 
        
        // Cell init
        for(int sl=0; sl<response.getCells().size(); sl++) {
            for(int i=0; i<response.getCells().get(sl).size(); i++) {
                cells[sl][i] = response.getCells().get(sl).get(i).getOwner();
            }
        }
        
        // Units init
        for(int u = 0; u<response.getUnits().size(); u++) {
            Coord coord = new Coord(response.getUnits().get(0).getPosition().getX(), 
                    response.getUnits().get(0).getPosition().getY());
            Unit unit = new Unit(coord, response.getUnits().get(0).getHealth(), 
                    response.getUnits().get(0).getKiller(),
                    response.getUnits().get(0).getOwner());
            units.add(unit);
        }
    }
    
    public boolean nearEmptyField() {
        Unit unit = units.get(0);
        
        if (unit.coord.x<ROWS && cells[unit.coord.x+1][unit.coord.y]==0) {
            return true;
        }
        if (unit.coord.x>0 && cells[unit.coord.x-1][unit.coord.y]==0) {
            return true;
        }
        if (unit.coord.y<COLS && cells[unit.coord.x][unit.coord.y+1]==0) {
            return true;
        }
        if (unit.coord.y>0 && cells[unit.coord.x][unit.coord.y-1]==0) {
            return true;
        }
        if (unit.coord.x<ROWS && unit.coord.y<COLS && cells[unit.coord.x+1][unit.coord.y+1]==0) {
            return true;
        }
        if (unit.coord.x<ROWS && unit.coord.y>0 && cells[unit.coord.x+1][unit.coord.y-1]==0) {
            return true;
        }
        if (unit.coord.x>0 && unit.coord.y<COLS && cells[unit.coord.x-1][unit.coord.y+1]==0) {
            return true;
        }
        if (unit.coord.x>0 && unit.coord.y>0 && cells[unit.coord.x+1][unit.coord.y-1]==0) {
            return true;
        }
        return false;
    }

    public List<CommonClass.Direction> findShortestPathToEmptyField() {
        List<CommonClass.Direction> result = new ArrayList<>();
        Unit unit = units.get(0);
        
        if (cells[unit.coord.x][unit.coord.y] > 0) {
            CommonClass.Direction bestDir = CommonClass.Direction.RIGHT;
            int bestSteps = Integer.MAX_VALUE;
            
            if (unit.coord.x < ROWS) {
                // próbáljuk lefele
                int steps = 0;
                int x = unit.coord.x;
                int y = unit.coord.y;
                while (++x < ROWS) {
                    ++steps;
                    if (cells[x][y]==0) break;   // pont nekimentem egy üres mezőnek
                    if (y < COLS && cells[x][y+1]==0) break; // a jobb alsó sarokban van egy üres mező
                    if (y > 0 && cells[x][y-1]==0) break;   // a bal alsó sarokban van egy üres mező
                }
                if (x < ROWS && steps < bestSteps) {
                    bestSteps = steps;
                    bestDir = CommonClass.Direction.DOWN;
                }
            }
            
            if (unit.coord.x > 0) {
                // próbáljuk felfele
                int steps = 0;
                int x = unit.coord.x;
                int y = unit.coord.y;
                while (--x > 0) {
                    ++steps;
                    if (cells[x][y]==0) break;   // pont nekimentem egy üres mezőnek
                    if (y < COLS && cells[x][y+1]==0) break; // a jobb felső sarokban van egy üres mező
                    if (y > 0 && cells[x][y-1]==0) break;   // a bal felső sarokban van egy üres mező
                }
                if (x > 0 && steps < bestSteps) {
                    bestSteps = steps;
                    bestDir = CommonClass.Direction.UP;
                }
            }
            
            if (unit.coord.y < COLS) {
                // próbáljuk jobbra
                int steps = 0;
                int x = unit.coord.x;
                int y = unit.coord.y;
                while (++y < COLS) {
                    ++steps;
                    if (cells[x][y]==0) break;   // pont nekimentem egy üres mezőnek
                    if (x < ROWS && cells[x+1][y]==0) break; // a jobb alsó sarokban van egy üres mező
                    if (x > 0 && cells[x-1][y]==0) break;   // a jobb felső sarokban van egy üres mező
                }
                if (y < COLS && steps < bestSteps) {
                    bestSteps = steps;
                    bestDir = CommonClass.Direction.RIGHT;
                }
            }
            
            if (unit.coord.y > 0) {
                // próbáljuk balra
                int steps = 0;
                int x = unit.coord.x;
                int y = unit.coord.y;
                while (--y > 0) {
                    ++steps;
                    if (cells[x][y]==0) break;   // pont nekimentem egy üres mezőnek
                    if (x < ROWS && cells[x+1][y]==0) break; // a bal alsó sarokban van egy üres mező
                    if (x > 0 && cells[x-1][y]==0) break;   // a bal felső sarokban van egy üres mező
                }
                if (y > 0 && steps < bestSteps) {
                    bestSteps = steps;
                    bestDir = CommonClass.Direction.LEFT;
                }
            }
            
            for(int i=0; i<bestSteps; ++i) {
                result.add(bestDir);
            }
        }
        
        return result;
    }

    /**
     * Keresztirányú áthaladáshoz keres útvonalakat, csak irány-listákat 
     * és az áthaladással elérhető terület-nyereségeket adja vissza
     * ezeket lehet majd a szimulátoron végigfuttatni
     * 
     * @return 
     */
    public List<Tuple<List<CommonClass.Direction>, Integer>> findCrossPaths() {
        List<Tuple<List<CommonClass.Direction>, Integer>> result = new ArrayList<>();
        Unit unit = units.get(0);
        
        int x = unit.coord.x;
        int y = unit.coord.y;
        int maxArea;
        
        // tételezzük fel, hogy nem a szélén vagyunk a pályának, elvileg ilyen nem fordulhatna elő
        assert x>0 && x<ROWS-1 && y>0 && y<COLS-1; 
        // tételezzük fel, hogy saját területen vagyunk
        assert cells[x][y] > 0;
        
        if (cells[x+1][y+1]==0 || cells[x+1][y-1]==0 || cells[x-1][y+1]==0 || cells[x-1][y-1]==0) {
            // átlósan kezdődik a pálya
            // 111
            // 1¤1
            // 11×
            
            // meddig tart vízszintesen/függőlegesen?
            int dirX=0, dirY=0, pX=0, pY=0, p1X, p1Y;
            if (cells[x+1][y+1]==0) {
                dirX = 1;
                dirY = 1;
                pX = x+1;
                pY = y+1;
            } else if (cells[x+1][y-1]==0) {
                dirX = 1;
                dirY = -1;
                pX = x+1;
                pY = y-1;
            } else if (cells[x-1][y+1]==0) {
                dirX = -1;
                dirY = 1;
                pX = x-1;
                pY = y+1;
            } else if (cells[x-1][y-1]==0) {
                dirX = -1;
                dirY = -1;
                pX = x-1;
                pY = y-1;
            }
            // Ide majd vissza kell térni
            p1X = pX;
            p1Y = pY;
            
            // szaladjunk végig függőlegesen, amíg nem találunk valamit
            while (pX > 0 && pX < ROWS) {
                pX += dirX;
                if (cells[pX][pY] > 0) break;
            }
            
            // szaladjunk végig vízszintesen
            while (pY > 0 && pY < COLS) {
                pY += dirY;
                if (cells[pX][pY] > 0) break;
            }            
            // most pX és pY a túlsó szélét mutatja az üres területnek
            
            // az üres területnek max. a felét lehet egy menetben elfoglalni
            maxArea = Math.abs((pX-p1X) * (pY-p1Y));
            
            // path-ok generálása
            // függőlegesen x sor, aztán vízszintesen végig
            for(int sor = 2; sor < Math.abs(p1X-pX)-2; ++sor ) {
                int area = (sor-1) * Math.abs(pY-p1Y);
                if (area > maxArea/2) {
                    area = maxArea-area;
                }
                Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps((int)Math.signum(pX-p1X)*sor, pY-p1Y, true), area);
                result.add(t);
            }

            // vízszintesen x oszlop, aztán függőlegesen végig
            for(int oszlop = 2; oszlop < Math.abs(p1Y-pY)-2; ++oszlop ) {
                int area = (oszlop-1) * Math.abs(pX-p1X);
                if (area > maxArea/2) {
                    area = maxArea-area;
                }
                Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps(pX-p1X, (int)Math.signum(pY-p1Y)*oszlop, false), area);
                result.add(t);
            }
            
        }
        
        return result;
    }
    
    /**
     * Útvonal-listát generál az előjelesen megadott X és Y irányú lépésszámból
     * Negatív x irány felfele lépést, pozitív lefele lépést jelent,
     * negatív y irány balra lépést, poritív jobbra lépést jelent
     * 
     * @param xSteps
     * @param ySteps
     * @return 
     */
    private List<CommonClass.Direction> createPathByXYSteps(int xSteps, int ySteps, boolean xFirst) {
        List<CommonClass.Direction> path = new ArrayList<>();
        List<CommonClass.Direction> pathX = new ArrayList<>();
        List<CommonClass.Direction> pathY = new ArrayList<>();
        
        for(int i=0; i<Math.abs(xSteps); ++i) {
            if (xSteps < 0) {
                pathX.add(CommonClass.Direction.UP);
            } else {
                pathX.add(CommonClass.Direction.DOWN);
            }
        }
        
        for(int i=0; i<Math.abs(ySteps); ++i) {
            if (ySteps < 0) {
                pathY.add(CommonClass.Direction.LEFT);
            } else {
                pathY.add(CommonClass.Direction.RIGHT);
            }
        }
        
        if (xFirst) {
            path.addAll(pathX);
            path.addAll(pathY);
        } else {
            path.addAll(pathY);
            path.addAll(pathX);
        }
        return path;
    }
}
