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
 * Implements an expression sequence, statment is evaluated
 * for side effects and the expression is evaluated as result.
 */

public class ESEQ extends Exp 
{
    public Stm stm;
    public Exp exp;
    public ESEQ(Stm s, Exp e) { stm=s; exp=e; }

    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	throw new Error("kids() not applicable to ESEQ");
    }
    public Exp build(LinkedList<Exp> kids)
    {
	throw new Error("build() not applicable to ESEQ");
    }
    public Temp accept(CodeVisitor v) { return v.visit(this); }
}
