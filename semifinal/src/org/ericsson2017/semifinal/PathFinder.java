package org.ericsson2017.semifinal;

import java.util.ArrayList;
import java.util.List;
import org.ericsson2017.protocol.semifinal.CommonClass;
import static org.ericsson2017.semifinal.Simulator.COLS;
import static org.ericsson2017.semifinal.Simulator.ROWS;
import static org.ericsson2017.semifinal.Simulator.MIN_AREA;

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
    
    
    //Megnézi a hogy a unit körül van-e üres mező
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
        int area = 0;
        int x = 0;
        int y = 0;
        
        assert cells[unit.coord.x][unit.coord.y] > 0;   // nem szabad, hogy üres mezőn álljunk
        
        CommonClass.Direction bestDir = CommonClass.Direction.RIGHT;
        int bestSteps = Integer.MAX_VALUE;
        int[][] cellsCopy = new int[ROWS][COLS];
        for(int i=0;i<ROWS;i++)
        {
            for(int j=0; j<COLS; j++)
            {
                cellsCopy[i][j] = cells[i][j];
            }
        }
        while(area < MIN_AREA)
        { 
            bestSteps = Integer.MAX_VALUE;
            if (unit.coord.x < ROWS) {
            // próbáljuk lefele
                int steps = 0;
                int tmpx = unit.coord.x;
                int tmpy = unit.coord.y;
                while (++tmpx < ROWS) {
                    ++steps;
                    if (cellsCopy[tmpx][tmpy]==0) break;   // pont nekimentem egy üres mezőnek
                    if (tmpy < COLS && cellsCopy[tmpx][tmpy+1]==0) {
                        tmpy = tmpy+1;
                        break;
                    } // a jobb alsó sarokban van egy üres mező
                    if (tmpy > 0 && cellsCopy[tmpx][tmpy-1]==0){
                        tmpy = tmpy-1;
                        break;
                    }   // a bal alsó sarokban van egy üres mező
                    
                }

                if (tmpx < ROWS && steps < bestSteps) {
                    bestSteps = steps;
                    x=tmpx;
                    y=tmpy;
                }
          }

            if (unit.coord.x > 0) {
                // próbáljuk felfele
                int steps = 0;
                int tmpx = unit.coord.x;
                int tmpy = unit.coord.y;
                while (--tmpx > 0) {
                    ++steps;
                    if (cellsCopy[tmpx][tmpy]==0) break;   // pont nekimentem egy üres mezőnek
                    if (tmpy < COLS && cellsCopy[tmpx][tmpy+1]==0) {
                        tmpy = tmpy+1;
                        break;
                    } // a jobb felső sarokban van egy üres mező
                    if (tmpy > 0 && cellsCopy[tmpx][tmpy-1]==0){
                        tmpy = tmpy-1;
                        break;
                    }   // a bal felső sarokban van egy üres mező
                }
                if (tmpx > 0 && steps < bestSteps) {
                    bestSteps = steps;
                    x=tmpx;
                    y=tmpy;
                    bestDir = CommonClass.Direction.UP;
                }
            }

            if (unit.coord.y < COLS) {
                // próbáljuk jobbra
                int steps = 0;
                int tmpx = unit.coord.x;
                int tmpy = unit.coord.y;
                boolean found = false;

                while (++tmpy < COLS && !found) {
                    ++steps;
                    if (cellsCopy[tmpx][tmpy]==0) break;   // pont nekimentem egy üres mezőnek
                    if (tmpx < ROWS && cellsCopy[tmpx+1][tmpy]==0) {
                        tmpx= tmpx+1;
                        break;
                    } // a jobb alsó sarokban van egy üres mező
                    if (tmpx > 0 && cellsCopy[tmpx-1][tmpy]==0) {
                        tmpx= tmpx-1;break;
                    }   // a jobb felső sarokban van egy üres mező
                }
                if (tmpy < COLS && steps < bestSteps) {
                    x=tmpx;
                    y=tmpy;
                    bestDir = CommonClass.Direction.RIGHT;
                    bestSteps = steps;
                    found = true;
                }
            }

            if (unit.coord.y > 0) {
                // próbáljuk balra
                int steps = 0;
                int tmpx = unit.coord.x;
                int tmpy = unit.coord.y;
                while (--tmpy > 0) {
                    ++steps;
                    if (cellsCopy[tmpx][tmpy]==0) break;   // pont nekimentem egy üres mezőnek
                    if (tmpx < ROWS && cellsCopy[tmpx+1][tmpy]==0) {
                        tmpx = tmpx+1;
                        break;
                    } // a bal alsó sarokban van egy üres mező
                    if (tmpx > 0 && cellsCopy[tmpx-1][tmpy]==0) {
                        tmpx=tmpx-1;
                        break;
                    }   // a bal felső sarokban van egy üres mező
                }
                if (tmpy > 0 && steps < bestSteps) {
                    bestSteps = steps;
                    x=tmpx;
                    y=tmpy;
                    bestDir = CommonClass.Direction.LEFT;
                }
            }
            
             // vízszintes/függőleges mozgással nem lett meg az "aréna"?
             //Körkörösen megkeresem a legelső üresmezőt.
             //TODO tesztelni
            if (bestSteps == Integer.MAX_VALUE) {
                System.out.println("Arena not found with horiz/vert search, trying other method");
                //printCells();
                
                x = unit.coord.x;
                y = unit.coord.y;
                boolean found = false;
                for(int i = 1; i<ROWS && !found;i++)
                {
                    int j = -i;
                    //Lent és fent
                    while(j<=i && !found)
                    {
                        //Fent
                        if((x-i)>0 && (y+j)>0 && cellsCopy[x-i][y+j] == 0)
                        {
                            x = x-i;
                            y = y+j;
                            found = true;
                            break;
                        }
                        
                        //Lent
                        if((x+i)>0 && (y+j)>0 && cellsCopy[x+i][y+j] == 0)
                        {
                            x = x+i;
                            y = y+j;
                            found = true;
                            break;
                        }
                        
                        //Balra
                        if((x+j)>0 && (y-i)>0 && cellsCopy[x+j][y-i] == 0)
                        {
                            x = x+j;
                            y = y+i;
                            found = true;
                            break;
                        }
                        
                        //Jobbra
                        if((x+j)>0 && (y+i)>0 && cellsCopy[x+j][y+i] == 0)
                        {
                            x = x+j;
                            y = y+i;
                            found = true;
                            break;
                        }
                        j++;
                    }
                }
                System.out.println("Átló:(" + x + " " + y + " )");
            }
            area = calculateArea(cellsCopy,x,y);
            System.out.println("area:" + area + "(" + x + " " + y + " )");
            if(area < MIN_AREA)
            {
                colorArea(cellsCopy,x,y, 1);
            }
        }
        
        result = calculateRoute(unit,x,y);
       
       //Az utolsó elemet kivesszük, mert a következő lépésben a simulator azt várja, hogy az üres mező mellett álljunk, és ne rajta.
       CommonClass.Direction lastItem = result.get(result.size() - 1);
       result.remove(lastItem);

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
        
        //TODO a menekülő útvonalak miatt nagy a valószínűsége, hogy nem sarkon vagyunk, ezért a balra/jobbra/le/fel irányokkal kéne kezdeni, és utána ez.
        //Ezért ez a megoldás nem teljesen optimális. A jelenlegi else ágba sose fogunk belefutni.
        int x = unit.coord.x;
        int y = unit.coord.y;
        int possibleDirections = 0;
        int possibleXDirections [] = new int[4];
        int possibleYDirections [] = new int [4];
        for(int i =0;i<4;i++)
        {
            possibleXDirections[i] = 0;
            possibleYDirections[i] = 0;
        }
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
                possibleXDirections[possibleDirections] = 1;
                possibleYDirections[possibleDirections] = 1;
                possibleDirections++;
            } else if (cells[x+1][y-1]==0) {    // balra lefele kezdődik
                possibleXDirections[possibleDirections] = 1;
                possibleYDirections[possibleDirections] = -1;
                possibleDirections++;
            } else if (cells[x-1][y+1]==0) {    // jobbra felfele kezdődiki
                possibleXDirections[possibleDirections] = -1;
                possibleYDirections[possibleDirections] = 1;
                possibleDirections++;
            } else if (cells[x-1][y-1]==0) {    // balra felfele kezdődik
                possibleXDirections[possibleDirections] = -1;
                possibleYDirections[possibleDirections] = -1;
                possibleDirections++;
            }
            for(int i = 0; i<4;i++)
            {
                //Csak annyit nézünk meg amerre tudunk is menni.
                if(possibleXDirections[i] == 0)
                {
                    break;
                }
                pX = x + possibleXDirections[i];
                pY = y + possibleYDirections[i];

                assert cells[pX][pY] == 0;      // az "aréna" egyik sarkában vagyunk

                // Ide majd vissza kell térni
                pXorig = pX;
                pYorig = pY;

                // szaladjunk végig függőlegesen, amíg nem találunk valamit
                while (pX > 0 && pX < ROWS) {
                    pX += possibleXDirections[i];
                    if (cells[pX+possibleXDirections[i]][pY] > 0) break;
                }

                // szaladjunk végig vízszintesen
                while (pY > 0 && pY < COLS) {
                    pY += possibleYDirections[i];
                    if (cells[pX][pY+possibleYDirections[i]] > 0) break;
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

                    Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps((int)Math.signum(pX-pXorig)*sor, pY-pYorig+2*possibleYDirections[i], true), area);
                    result.add(t);
                }

                // vízszintesen x oszlop, aztán függőlegesen végig
                for(int oszlop = 2; oszlop < Math.abs(pYorig-pY)-2; ++oszlop ) {
                    int area = (oszlop-1) * Math.abs(pX-pXorig);
                    if (area > maxArea/2) {
                        area = maxArea-area;
                    }
    //System.out.println("Generate "+ (Math.signum(pY-pYorig)*oszlop) + ":" + (pX-pXorig));
                    Tuple<List<CommonClass.Direction>, Integer> t = new Tuple<>(createPathByXYSteps(pX-pXorig+2*possibleXDirections[i], (int)Math.signum(pY-pYorig)*oszlop, false), area);
                    result.add(t);
                }  
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

    /**
     * Menekülő utakat keres a megadott irányra merőlegesen
     * 
     * A jelenlegi implementáció csak a 2 merőleges irányban gyárt le 1-1 utat az aréna széléig!
     * 
     * @param prohibitedDirection
     * @return 
     */
    List<List<CommonClass.Direction>> findEscapeRoutes(CommonClass.Direction prohibitedDirection) {
        List<List<CommonClass.Direction>> result = new ArrayList<>();
        
        Unit unit = units.get(0);
        int origX = unit.getCoord().getX();
        int origY = unit.getCoord().getY();
        
        CommonClass.Direction[] targetDirections = new CommonClass.Direction[2];
        
        switch (prohibitedDirection) {
            case UP:
            case DOWN:
                targetDirections[0] = CommonClass.Direction.RIGHT;
                targetDirections[1] = CommonClass.Direction.LEFT;
                break;
            case RIGHT:
            case LEFT:
                targetDirections[0] = CommonClass.Direction.UP;
                targetDirections[1] = CommonClass.Direction.DOWN;
                break;
        }
        
        for(CommonClass.Direction dir : targetDirections) {
            // addig kell menni az adott irányban, amíg ki nem érünk az arénából
            int uX = origX;
            int uY = origY;
            List<CommonClass.Direction> resultElement = new ArrayList<>();
            
            while (uX > 0 && uX < ROWS && uY > 0 && uY < COLS) {
                switch(dir) {
                    case UP:
                        --uX;
                        break;
                    case DOWN:
                        ++uX;
                        break;
                    case RIGHT:
                        ++uY;
                        break;
                    case LEFT:
                        --uY;
                        break;
                }
                resultElement.add(dir);
                if (cells[uX][uY] > 0) break;
            }
            
            result.add(resultElement);
        }
        
        return result;
    }

    private void printCells() {
        for(int x=0; x<ROWS; x++) {
            for(int y=0; y<COLS; y++) {
                String cellValue = " ";
                
                for(Unit u : units) {
                    if (u.getCoord().getX() == x && u.getCoord().getY() == y) {
                        cellValue = "¤";
                    }
                }
                                
                if (cellValue.equals(" ")) {
                    cellValue = Integer.toString(cells[x][y]);
                }
                
                System.out.print( cellValue );
            }
            
            System.out.println();
        }
    }    

    //Kiszámolja a téglalap területét, amibe az x,y is beletartozik
    private int calculateArea(int[][] cellsCopy, int x, int y) {
        int area = 0;
        int tmpx = x;
        int tmpy = y;
        //Lefelé összegzem a területet
        while(cellsCopy[tmpx][tmpy] == 0 && tmpx<ROWS)
        {
        
            //Jobbra
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy<COLS)
            {
                System.out.print("("+tmpx+","+tmpy+")");
                area++;
                tmpy++;
            }

            //Balra
            tmpy = y-1;
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy>0)
            {
                System.out.print("("+tmpx+","+tmpy+")");
                area++;
                tmpy--;
            }
            tmpy = y;
            tmpx++;
        }
        System.out.println("Terulet:"+ area);
        
        tmpx = x-1;
        tmpy = y;
        
        //Felfelé összegzem a területet
        while(cellsCopy[tmpx][tmpy] == 0 && tmpx<0)
        {
        
            //Jobbra
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy<COLS)
            {
                area++;
                tmpy++;
            }

            //Balra
            tmpy = y-1;
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy>0)
            {
                area++;
                tmpy--;
            }
            tmpy = y;
            tmpx--;
        }
        
        return area;
    }

    private void colorArea(int[][] cellsCopy, int x, int y, int color) {
        int tmpx = x;
        int tmpy = y;
        //Lefelé összegzem a területet
        while(cellsCopy[tmpx][tmpy] == 0 && tmpx<ROWS)
        {
        
            //Jobbra
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy<COLS)
            {
                cellsCopy[tmpx][tmpy] = color;
                tmpy++;
            }

            //Balra
            tmpy = y-1;
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy>0)
            {
                cellsCopy[tmpx][tmpy] = color;
                tmpy--;
            }
            tmpx++;
            tmpy = y;
        }
        
        tmpx = x-1;
        
        //Felfelé összegzem a területet
        while(cellsCopy[tmpx][tmpy] == 0 && tmpx<0)
        {
        
            //Jobbra
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy<COLS)
            {
                cellsCopy[tmpx][tmpy] = color;
                tmpy++;
            }

            //Balra
            tmpy = y-1;
            while(cellsCopy[tmpx][tmpy] == 0 && tmpy>0)
            {
                cellsCopy[tmpx][tmpy] = color;
                tmpy--;
            }
            tmpx--;
            tmpy = y;
        }
    }

    private List<CommonClass.Direction> calculateRoute(Unit unit, int finishX, int finishY) {
       
        List<CommonClass.Direction> result = new ArrayList<>();
        int x = unit.coord.x;
        int y = unit.coord.y;
        int dirXnumber = y>finishY ? -1 : 1;
        int dirYnumber = x>finishX ? -1 : 1;
        CommonClass.Direction dirX = y>finishY ? CommonClass.Direction.LEFT : CommonClass.Direction.RIGHT;
        CommonClass.Direction dirY = x>finishX ? CommonClass.Direction.UP : CommonClass.Direction.DOWN;
        while(x!=finishX)
        {
            result.add(dirY);
            x+= dirYnumber;
        }
        
        while(y!= finishY)
        {
            result.add(dirX);
            y+= dirXnumber;
        }
        return result;
    }
}
