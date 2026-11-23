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
import java.util.List;
import java.util.LinkedList;

/**
 * Jump (transfer control) to address e.
 */

public class JUMP extends Stm 
{
    public Exp exp;
    public LinkedList<Label> targets;
    public JUMP(Exp e, LinkedList<Label> t) { exp=e; targets=t; }
    public JUMP(Label target) 
	{
		exp = new NAME(target);
		targets = new LinkedList<Label>();
		targets.addFirst(target);
    }

    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	LinkedList<Exp> l = new LinkedList<Exp>(); l.add(exp); return l;
    }
    public Stm build(LinkedList<Exp> kids)
    {
	return new JUMP(kids.get(0), targets);
    }
    public void accept(CodeVisitor v) { v.visit(this); }
}
