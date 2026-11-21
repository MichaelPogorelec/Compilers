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
import Temp.Label;
import java.util.LinkedList;

/**
 * Implements a move instruction.
 */

public class MOVE extends Stm 
{
    public Exp dst, src;
    public MOVE(Exp d, Exp s) { dst=d; src=s; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	if (dst instanceof TEMP) { LinkedList<Exp> l = new LinkedList<Exp>(); l.add(src); return l; }
	if (dst instanceof MEM) { LinkedList<Exp> l = new LinkedList<Exp>(); l.add(((MEM)dst).exp); l.add(src); return l; }
	throw new Error("kids() of MOVE: destination must be TEMP or MEM");
    }
    public Stm build(LinkedList<Exp> kids)
    {
	if (dst instanceof TEMP) return new MOVE(dst, kids.get(0));
	if (dst instanceof MEM) return new MOVE(new MEM(kids.get(0)), kids.get(1));
	throw new Error("build() of MOVE: destination must be TEMP or MEM");
    }
    public void accept(CodeVisitor v) { v.visit(this); }
}
