/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ericsson2017.semifinal;

/**
 *
 * @author norbi
 */
public class Tuple<X,Y> {
    public final X first;
    public final Y second;

    public Tuple(X x, Y y) {
        this.first = x;
        this.second = y;
    }
}