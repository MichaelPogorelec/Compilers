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
 * Define constant value as current code address.
 */

public class LABEL extends Stm 
{ 
    public Label label;
    public LABEL(Label l) { label=l; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	return new LinkedList<Exp>();
    }
    public Stm build(LinkedList<Exp> kids)
    {
	return this;
    }
    public void accept(CodeVisitor v) { v.visit(this); }
}
