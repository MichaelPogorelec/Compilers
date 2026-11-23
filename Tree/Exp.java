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
import java.util.LinkedList;
import Temp.Temp;

/**
 * Abstract class that represents an expression in the Tree language.
 */

public abstract class Exp implements Hospitable
{
    public abstract void accept(IntVisitor v, int d);
    /** The subexpressions, left to right, that Canon may reorder. */
    public abstract LinkedList<Exp> kids();
    /** A copy of this node with its subexpressions replaced by kids. */
    public abstract Exp build(LinkedList<Exp> kids);
    public abstract Temp accept(CodeVisitor v);
}
