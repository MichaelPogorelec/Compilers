/**
 * COSC 4400 - Project 7
 * Modified for the AST-to-Tree translator.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Translate;
import Temp.Label;

/**
 * Stands for an "expression", represented as a Tree.Exp.
 */

public class Ex extends Exp {
    Tree.Exp exp;
    Ex(Tree.Exp e)  { exp = e; }
    Tree.Exp unEx() { return exp; }
    Tree.Stm unNx() {
	// A constant has no side effects, so there is nothing to execute.
	if (exp instanceof Tree.CONST)
	    return null;
	return new Tree.EXP(exp);
    }
    Tree.Stm unCx(Label t, Label f) {
	// A constant condition is decided now: jump straight to the target.
	if (exp instanceof Tree.CONST)
	{
	    if (((Tree.CONST) exp).value == 0)
		return new Tree.JUMP(f);
	    return new Tree.JUMP(t);
	}
	return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);
    }
}
