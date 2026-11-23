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
 * An abstract class that represents a statment in the Tree language.
 */

abstract public class Stm implements Hospitable
{
    public abstract void accept(IntVisitor v, int d);
    /** The subexpressions, left to right, that Canon may reorder. */
    public abstract LinkedList<Exp> kids();
    /** A copy of this node with its subexpressions replaced by kids. */
    public abstract Stm build(LinkedList<Exp> kids);
    public abstract void accept(CodeVisitor v);
}
