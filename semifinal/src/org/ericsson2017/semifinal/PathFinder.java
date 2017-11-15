package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import static org.ericsson2017.semifinal.Simulator.COLS;
import static org.ericsson2017.semifinal.Simulator.ROWS;

/**
 *
 * @author norbi
 */
public class PathFinder {
    public int[][] cells;
    public List<Unit> units;
    ServerResponseParser serverResponseParser;
    
    public PathFinder(ServerResponseParser serverResponseParser) {
        cells = new int[ROWS][COLS];
        units = new ArrayList<>(); 
        this.serverResponseParser = serverResponseParser;
        
        responseChanged();
    }
    
    public final void responseChanged() {
        cells = serverResponseParser.getCells();
        units = serverResponseParser.getUnits();
        
        System.out.println("PathFinder response changed. Unit pos: "+units.get(0).coord.toString());
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
        if (unit.coord.x>0 && unit.coord.y>0 && cells[unit.coord.x-1][unit.coord.y-1]==0) {
            return true;
        }
        return false;
    }

    public List<CommonClass.Direction> findShortestPathToEmptyField() {
        List<CommonClass.Direction> result = new ArrayList<>();
        Unit unit = units.get(0);
        
        assert cells[unit.coord.x][unit.coord.y] > 0;   // nem szabad, hogy üres mezőn álljunk
        
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
        
        // vízszintes/függőleges mozgással nem lett meg az "aréna"?
        if (bestSteps == Integer.MAX_VALUE) {
            System.out.println("Arena not found with horiz/vert search, trying other method");
            // keresem a befoglaló téglalapot
            int l=COLS, r=0, t=ROWS, b=0;
            for(int co=0; co<COLS; ++co) {
                for(int ro=0; ro<ROWS; ++ro) {
                    if (cells[ro][co]==0) {
                        t = Math.min(ro, t);
                        b = Math.max(ro, b);
                        l = Math.min(co, l);
                        r = Math.max(co, r);
                    }
                }
            }
            System.out.println("Arena coords: ("+t+":"+l+") - ("+b+":"+r+")");
            // melyik sarka van legközelebb?
            Coord nearestEdge = findNearestEdge(t, l, b, r, 0);
            System.out.println("Nearest edge: "+nearestEdge);
            System.out.println("Create steps: "+
                    (nearestEdge.getX() - unit.coord.getX() - (int)Math.signum(nearestEdge.getX() - unit.coord.getX())) + ":" +
                    (nearestEdge.getY() - unit.coord.getY() - (int)Math.signum(nearestEdge.getY() - unit.coord.getY()))
                    );
            return createPathByXYSteps(
                    nearestEdge.getX() - unit.coord.getX() - (int)Math.signum(nearestEdge.getX() - unit.coord.getX()), 
                    nearestEdge.getY() - unit.coord.getY() - (int)Math.signum(nearestEdge.getY() - unit.coord.getY()), 
                    true
                    );
        }
        
        // eggyel kevesebbet kell az adott irányba lépni, mert nem kell "belemenni" a üres mezőbe
        // hanem előtte meg kell állni
        for(int i=0; i<bestSteps-1; ++i) {
            result.add(bestDir);
        }
        System.out.println("Found path to arena in "+(bestSteps-1)+" steps in direction "+bestDir);

        return result;
    }

    /**
     * Keresztirányú áthaladáshoz keres útvonalakat, csak irány-listákat 
     * és az áthaladással elérhető terület-nyereségeket adja vissza
     * ezeket lehet majd a szimulátoron végigfuttatni
     * 
     * @return 
     * @throws java.lang.Throwable 
     */
    public List<Tuple<List<CommonClass.Direction>, Integer>> findCrossPaths() throws Throwable {
        List<Tuple<List<CommonClass.Direction>, Integer>> result = new ArrayList<>();
        Unit unit = units.get(0);
        
        int x = unit.coord.x;
        int y = unit.coord.y;
        int maxArea;
        
        // tételezzük fel, hogy nem a szélén vagyunk a pályának, elvileg ilyen nem fordulhat elő
        assert x>0 && x<ROWS-1 && y>0 && y<COLS-1; 
        // tételezzük fel, hogy saját területen vagyunk
        assert cells[x][y] > 0;
        
        // átlósan kezdődik a pálya?
        if (cells[x+1][y+1]==0 || cells[x+1][y-1]==0 || cells[x-1][y+1]==0 || cells[x-1][y-1]==0) {            
            // 1111
            // 1¤11
            // 1100
            // 1100
            
            // meddig tart vízszintesen/függőlegesen?
            int dirX=0, dirY=0, pX=0, pY=0, pXorig, pYorig;
            if (cells[x+1][y+1]==0) {   // jobbra lefele kezdődik
                dirX = 1;
                dirY = 1;
            } else if (cells[x+1][y-1]==0) {    // balra lefele kezdődik
                dirX = 1;
                dirY = -1;
            } else if (cells[x-1][y+1]==0) {    // jobbra felfele kezdődik
                dirX = -1;
                dirY = 1;
            } else if (cells[x-1][y-1]==0) {    // balra felfele kezdődik
                dirX = -1;
                dirY = -1;
            }
            pX = x + dirX;
            pY = y + dirY;

            assert cells[pX][pY] == 0;      // az "aréna" egyik sarkában vagyunk
            
            // Ide majd vissza kell térni
            pXorig = pX;
            pYorig = pY;
            
            // szaladjunk végig függőlegesen, amíg nem találunk valamit
            while (pX > 0 && pX < ROWS) {
                pX += dirX;
                if (cells[pX+dirX][pY] > 0) break;
            }
            
            // szaladjunk végig vízszintesen
            while (pY > 0 && pY < COLS) {
                pY += dirY;
                if (cells[pX][pY+dirY] > 0) break;
            }            
            // most pX és pY a túlsó szélét mutatja az üres területnek

            // az üres területnek max. a felét lehet egy menetben elfoglalni
            maxArea = Math.abs((pX-pXorig) * (pY-pYorig));

            // path-ok generálása
            // függőlegesen x sor, aztán vízszintesen végig
            for(int sor = 2; sor < Math.abs(pXorig-pX)-2; ++sor ) {
                int area = (sor-1) * Math.abs(pY-pYorig);
                if (area > maxArea/2) {
                    area = maxArea-area;
                }

                Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps((int)Math.signum(pX-pXorig)*sor, pY-pYorig+2*dirY, true), area);
                result.add(t);
            }

            // vízszintesen x oszlop, aztán függőlegesen végig
            for(int oszlop = 2; oszlop < Math.abs(pYorig-pY)-2; ++oszlop ) {
                int area = (oszlop-1) * Math.abs(pX-pXorig);
                if (area > maxArea/2) {
                    area = maxArea-area;
                }
//System.out.println("Generate "+ (Math.signum(pY-pYorig)*oszlop) + ":" + (pX-pXorig));
                Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps(pX-pXorig+2*dirX, (int)Math.signum(pY-pYorig)*oszlop, false), area);
                result.add(t);
            }

        } else {
            // pont nekimentem a pálya oldalának
            throw new Throwable("Not implemented");
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

    private Coord findNearestEdge(int top, int left, int bottom, int right, int unitNum) {
        int uX = units.get(unitNum).coord.getX();
        int uY = units.get(unitNum).coord.getY();
        
        double distTL = calculateDistance(new Coord(top, left), new Coord(uX, uY));
        double distBL = calculateDistance(new Coord(bottom, left), new Coord(uX, uY));
        double distTR = calculateDistance(new Coord(top, right), new Coord(uX, uY));
        double distBR = calculateDistance(new Coord(bottom, right), new Coord(uX, uY));
        
        Coord result = new Coord(top, left);
        double nearest = distTL;
        if (distBL < nearest) {
            result = new Coord(bottom, left);
            nearest = distBL;
        }
        if (distTR < nearest) {
            result = new Coord(top, right);
            nearest = distTR;
        }
        if (distBR < nearest) result = new Coord(bottom, right);
        
        return result;
    }

    private double calculateDistance(Coord coord1, Coord coord2) {
        return Math.hypot(coord1.getX()-coord2.getX(), coord1.getY()-coord2.getY());
    }

    public boolean inEmptyField() {
        Unit unit = units.get(0);
        return cells[unit.getCoord().getX()][unit.getCoord().getY()] == 0;
    }

    public List<CommonClass.Direction> findUnitLastDirection() {
        Unit unit = units.get(0);
        List<CommonClass.Direction> result = new ArrayList<>();
        
        result.add(unit.dir == null ? CommonClass.Direction.DOWN : unit.dir);
        
        return result;
    }
}
