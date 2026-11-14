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
import Temp.Temp;
import Temp.Label;
import Tree.*;
import java.util.List;

/**
 * Represents an If-Then-Else Expression, a special case that can be 
 * an expression or statement depending on it's use.
 */

public class IfThenElseExp extends Exp {
    Exp cond, trueExp, falseExp;

    private static Tree.Exp CONST(int value)  { return new Tree.CONST(value); }
    private static Tree.Exp NAME(Label label) {	return new Tree.NAME(label); }
    private static Tree.Exp TEMP(Temp temp)   {	return new Tree.TEMP(temp); }
    private static Tree.Exp BINOP(int binop, Tree.Exp left, Tree.Exp right) 
    {   return new Tree.BINOP(binop, left, right);   }
    private static Tree.Exp MEM(Tree.Exp exp) {	return new Tree.MEM(exp); }
    private static Tree.Exp CALL(Tree.Exp func, List<Tree.Exp> args) 
    {	return new Tree.CALL(func, args);   }
    private static Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp)
    {
	if (stm == null) return exp;
	return new Tree.ESEQ(stm, exp);
    }
	
    private static Tree.Stm MOVE(Tree.Exp dst, Tree.Exp src) 
    {
	return new Tree.MOVE(dst, src);
    }
    private static Tree.Stm EXP(Tree.Exp exp) {	return new Tree.EXP(exp); }
    private static Tree.Stm JUMP(Label target) 
    {	return new Tree.JUMP(target);   }
    private static Tree.Stm CJUMP(int relop, Tree.Exp l, Tree.Exp r,
				  Label t, Label f)
    {	return new Tree.CJUMP(relop, l, r, t, f);   }
    private static Tree.Stm SEQ(Tree.Stm left, Tree.Stm right) 
    {
	if (left == null)
	    return right;
	if (right == null)
	    return left;
	return new Tree.SEQ(left, right);
    }
    private static Tree.Stm LABEL(Label label) 
    {
	return new Tree.LABEL(label);
    }


    IfThenElseExp(Exp cExp, Exp tExp, Exp fExp) {
	cond = cExp; 
	trueExp = tExp; 
	falseExp = fExp;
    }

    /** True if e is the constant expression 0 (false). */
    private static boolean isFalse(Exp e)
    {
	return (e instanceof Ex) && (((Ex) e).exp instanceof Tree.CONST)
	    && ((Tree.CONST) ((Ex) e).exp).value == 0;
    }

    /** True if e is a nonzero constant expression (true). */
    private static boolean isTrue(Exp e)
    {
	return (e instanceof Ex) && (((Ex) e).exp instanceof Tree.CONST)
	    && ((Tree.CONST) ((Ex) e).exp).value != 0;
    }

    /**
     * Used when this expression is a condition (e.g. the test of an if or
     * while): each branch becomes jumps to tt (true) or ff (false), so no
     * 0/1 value is ever materialized.
     */
    Tree.Stm unCx(Label tt, Label ff) {
	// !c : swap the targets
	if (isFalse(trueExp) && isTrue(falseExp))
	    return cond.unCx(ff, tt);
	// a && b : a false means false, a true means test b
	if (isFalse(falseExp))
	{
	    Label t = new Label();
	    return SEQ(cond.unCx(t, ff),
		       SEQ(LABEL(t), trueExp.unCx(tt, ff)));
	}
	// a || b : a true means true, a false means test b
	if (isTrue(trueExp))
	{
	    Label f = new Label();
	    return SEQ(cond.unCx(tt, f),
		       SEQ(LABEL(f), falseExp.unCx(tt, ff)));
	}
	Label t = new Label();
	Label f = new Label();
	return SEQ(cond.unCx(t, f),
		   SEQ(LABEL(t),
		       SEQ(trueExp.unCx(tt, ff),
			   SEQ(LABEL(f), falseExp.unCx(tt, ff)))));
    }

    Tree.Exp unEx() {
        Tree.Exp tExp = trueExp.unEx();
	Tree.Exp fExp = falseExp.unEx();
	Temp r = new Temp();
	Label t = new Label();
	Label f = new Label();
	Label join = new Label();
	return ESEQ(SEQ(SEQ(cond.unCx(t, f),
			    SEQ(SEQ(LABEL(t),
				    SEQ(MOVE(TEMP(r), tExp),
					JUMP(join))),
				SEQ(LABEL(f),
				    SEQ(MOVE(TEMP(r), fExp),
					JUMP(join))))),
			LABEL(join)),
		    TEMP(r));
    }

    Tree.Stm unNx() {
        Tree.Stm tStm = trueExp.unNx();
        Tree.Stm fStm = falseExp.unNx();
	Label t = new Label();
	Label f = new Label();
	// if-then with no else part: fall through to the false label
	if (fStm == null)
	    return SEQ(cond.unCx(t, f), SEQ(LABEL(t), SEQ(tStm, LABEL(f))));
	Label join = new Label();
	tStm = SEQ(SEQ(LABEL(t), tStm), JUMP(join));
	fStm = SEQ(SEQ(LABEL(f), fStm), JUMP(join));
	return SEQ(SEQ(cond.unCx(t, f), SEQ(tStm, fStm)), LABEL(join));
    }
}
