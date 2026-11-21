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
 * Sequence: statement 1 followed by statment 2.
 */

public class SEQ extends Stm 
{
    public Stm left, right;
    public SEQ(Stm l, Stm r) { left=l; right=r; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	throw new Error("kids() not applicable to SEQ");
    }
    public Stm build(LinkedList<Exp> kids)
    {
	throw new Error("build() not applicable to SEQ");
    }
    public void accept(CodeVisitor v) { v.visit(this); }
}
