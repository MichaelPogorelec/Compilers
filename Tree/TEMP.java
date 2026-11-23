/**
 * COSC 4400 - Project 8
 * Tree IR node, extended with kids()/build() for Canon and accept(CodeVisitor)
 * for the instruction selector.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Tree;
import Temp.Temp;
import java.util.LinkedList;

/**
 * A temporary t.
 */

public class TEMP extends Exp 
{
    public Temp temp;
    public TEMP(Temp t) 
	{ 
		temp=t; 
		//System.out.println("\n Call to TEMP : "+t.toString());
	}
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	return new LinkedList<Exp>();
    }
    public Exp build(LinkedList<Exp> kids)
    {
	return this;
    }
    public Temp accept(CodeVisitor v) { return v.visit(this); }
}
