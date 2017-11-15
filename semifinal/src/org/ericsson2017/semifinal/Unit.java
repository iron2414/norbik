/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;

import org.ericsson2017.protocol.semifinal.CommonClass;

/**
 *
 * @author norbi
 */
public class Unit
{
    public Coord coord;
    public CommonClass.Direction dir;
    public int health;
    public int killer;
    public int owner;
    
    public Unit(Coord coord, int health, int killer, int owner, CommonClass.Direction dir) {
        this.coord = coord;
        this.health = health;
        this.killer = killer;
        this.owner = owner;
        this.dir = dir;
    }

    public Coord getCoord() {
        return coord;
    }

    public CommonClass.Direction getDir() {
        return dir;
    }

    public int getHealth() {
        return health;
    }

    public int getKiller() {
        return killer;
    }

    public int getOwner() {
        return owner;
    }
}
