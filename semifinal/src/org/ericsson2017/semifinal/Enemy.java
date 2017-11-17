package org.ericsson2017.semifinal;

import org.ericsson2017.protocol.semifinal.CommonClass;

/**
 *
 * @author norbi
 */
public class Enemy 
{
    protected Coord coord;
    protected CommonClass.Direction dirX;
    protected CommonClass.Direction dirY;
    
    public Enemy(Coord coord, CommonClass.Direction dirX, CommonClass.Direction dirY) {
        this.coord = coord;
        this.dirX = dirX;
        this.dirY = dirY;
    }
	
	public Enemy copy() {
		return new Enemy(coord.copy(), dirX, dirY);
	}
    
    public Coord getCoord() {
        return coord;
    }

    public CommonClass.Direction getDirX() {
        return dirX;
    }

    public CommonClass.Direction getDirY() {
        return dirY;
    }
}
