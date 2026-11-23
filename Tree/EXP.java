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
 * Evaluates the expression and discards the result.
 */

public class EXP extends Stm 
{
    public Exp exp; 
    public EXP(Exp e) { exp=e; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	LinkedList<Exp> l = new LinkedList<Exp>(); l.add(exp); return l;
    }
    public Stm build(LinkedList<Exp> kids)
    {
	return new EXP(kids.get(0));
    }
    public void accept(CodeVisitor v) { v.visit(this); }
}
