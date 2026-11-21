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
 * Implements a procedure call, application of a function to an
 * argument list.
 */

public class CALL extends Exp 
{
    public Exp func;
    public List<Exp> args;
    public CALL(Exp f, List<Exp> a) { func=f; args=a; }
    public void accept(IntVisitor v, int d) { v.visit(this, d); }
    public LinkedList<Exp> kids()
    {
	LinkedList<Exp> l = new LinkedList<Exp>(); l.add(func); l.addAll(args); return l;
    }
    public Exp build(LinkedList<Exp> kids)
    {
	LinkedList<Exp> rest = new LinkedList<Exp>(kids); Exp f = rest.removeFirst(); return new CALL(f, rest);
    }
    public Temp accept(CodeVisitor v) { return v.visit(this); }
}
