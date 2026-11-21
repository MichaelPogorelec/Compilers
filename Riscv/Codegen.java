/**
 * COSC 4400 - Project 8
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
package Riscv;

import java.util.LinkedList;
import java.util.List;

import Assem.Instr;
import Assem.LABEL;
import Assem.MOVE;
import Assem.OPER;
import Temp.Label;
import Temp.Temp;
import Tree.BINOP;
import Tree.CALL;
import Tree.CJUMP;
import Tree.CONST;
import Tree.ESEQ;
import Tree.EXP;
import Tree.JUMP;
import Tree.MEM;
import Tree.NAME;
import Tree.SEQ;
import Tree.TEMP;

public class Codegen extends Tree.CodeVisitor
{
    /** The instructions selected so far, in order. */
    public LinkedList<Instr> ilist = new LinkedList<Instr>();

    private final RiscvFrame frame;

    public Codegen(RiscvFrame frame)
    {
	this.frame = frame;
    }

    /* ---------------------------------------------------------------- */
    /* Helpers                                                          */
    /* ---------------------------------------------------------------- */

    private void emit(Instr i)
    {
	ilist.add(i);
    }

    private static Temp[] L(Temp... temps)
    {
	return temps;
    }

    /** True for a constant that fits a 12-bit signed immediate. */
    private static boolean fits12(Tree.Exp e)
    {
	if (!(e instanceof CONST))
	    return false;
	int v = ((CONST) e).value;
	return v >= -2048 && v <= 2047;
    }

    private static int value(Tree.Exp e)
    {
	return ((CONST) e).value;
    }

    /** True for a constant 2^k (k >= 0). */
    private static boolean isPow2(Tree.Exp e)
    {
	if (!(e instanceof CONST))
	    return false;
	int v = ((CONST) e).value;
	return v > 0 && (v & (v - 1)) == 0;
    }

    private static int log2(Tree.Exp e)
    {
	return Integer.numberOfTrailingZeros(((CONST) e).value);
    }

    private Temp munch(Tree.Exp e)
    {
	return e.accept(this);
    }

    /* ---------------------------------------------------------------- */
    /* Statements                                                       */
    /* ---------------------------------------------------------------- */

    /** Canonical code has no SEQ nodes; tolerate one by visiting in order. */
    public void visit(SEQ n)
    {
	n.left.accept(this);
	n.right.accept(this);
    }

    public void visit(Tree.LABEL n)
    {
	emit(new LABEL(n.label + ":", n.label));
    }

    public void visit(JUMP n)
    {
	if (n.exp instanceof NAME)
	    emit(new OPER("j `j0", null, null, n.targets));
	else
	    emit(new OPER("jalr `s0", null, L(munch(n.exp)), n.targets));
    }

    private static String branch(int relop)
    {
	switch (relop)
	{
	case CJUMP.EQ:  return "beq";
	case CJUMP.NE:  return "bne";
	case CJUMP.LT:  return "blt";
	case CJUMP.GT:  return "bgt";
	case CJUMP.LE:  return "ble";
	case CJUMP.GE:  return "bge";
	case CJUMP.ULT: return "bltu";
	case CJUMP.ULE: return "bleu";
	case CJUMP.UGT: return "bgtu";
	case CJUMP.UGE: return "bgeu";
	}
	throw new Error("bad relop " + relop);
    }

    public void visit(CJUMP n)
    {
	Temp l = munch(n.left);
	Temp r = munch(n.right);
	LinkedList<Label> targets = new LinkedList<Label>();
	targets.add(n.iftrue);
	targets.add(n.iffalse);
	emit(new OPER(branch(n.relop) + " `s0, `s1, `j0", null, L(l, r),
		      targets));
    }

    public void visit(Tree.MOVE n)
    {
	if (n.dst instanceof MEM)
	{
	    Tree.Exp addr = ((MEM) n.dst).exp;
	    if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS)
	    {
		BINOP b = (BINOP) addr;
		if (fits12(b.right))
		{
		    Temp a = munch(b.left);
		    Temp v = munch(n.src);
		    emit(new OPER("sw `s1, " + value(b.right) + "(`s0)",
				  null, L(a, v), null));
		    return;
		}
		if (fits12(b.left))
		{
		    Temp a = munch(b.right);
		    Temp v = munch(n.src);
		    emit(new OPER("sw `s1, " + value(b.left) + "(`s0)",
				  null, L(a, v), null));
		    return;
		}
	    }
	    Temp a = munch(addr);
	    Temp v = munch(n.src);
	    emit(new OPER("sw `s1, 0(`s0)", null, L(a, v), null));
	}
	else if (n.dst instanceof TEMP)
	{
	    Temp s = munch(n.src);
	    emit(new MOVE("mv `d0, `s0", ((TEMP) n.dst).temp, s));
	}
	else
	    throw new Error("MOVE destination must be TEMP or MEM");
    }

    public void visit(EXP n)
    {
	munch(n.exp);
    }

    /* ---------------------------------------------------------------- */
    /* Expressions                                                      */
    /* ---------------------------------------------------------------- */

    public Temp visit(TEMP n)
    {
	return n.temp;
    }

    public Temp visit(CONST n)
    {
	if (n.value == 0)
	    return RiscvFrame.ZERO;
	Temp d = new Temp();
	emit(new OPER("li `d0, " + n.value, L(d), null, null));
	return d;
    }

    public Temp visit(NAME n)
    {
	Temp d = new Temp();
	emit(new OPER("la `d0, " + n.label, L(d), null, null));
	return d;
    }

    public Temp visit(MEM n)
    {
	Tree.Exp addr = n.exp;
	if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS)
	{
	    BINOP b = (BINOP) addr;
	    if (fits12(b.left))
	    {
		Temp s = munch(b.right);
		Temp d = new Temp();
		emit(new OPER("lw `d0, " + value(b.left) + "(`s0)",
			      L(d), L(s), null));
		return d;
	    }
	    if (fits12(b.right))
	    {
		Temp s = munch(b.left);
		Temp d = new Temp();
		emit(new OPER("lw `d0, " + value(b.right) + "(`s0)",
			      L(d), L(s), null));
		return d;
	    }
	}
	Temp s = munch(addr);
	Temp d = new Temp();
	emit(new OPER("lw `d0, 0(`s0)", L(d), L(s), null));
	return d;
    }

    /** op Rd, Rs, imm */
    private Temp immediate(String op, Tree.Exp operand, int imm)
    {
	Temp s = munch(operand);
	Temp d = new Temp();
	emit(new OPER(op + " `d0, `s0, " + imm, L(d), L(s), null));
	return d;
    }

    /** op Rd, Rs1, Rs2 */
    private Temp register(String op, Tree.Exp left, Tree.Exp right)
    {
	Temp l = munch(left);
	Temp r = munch(right);
	Temp d = new Temp();
	emit(new OPER(op + " `d0, `s0, `s1", L(d), L(l, r), null));
	return d;
    }

    public Temp visit(BINOP n)
    {
	Tree.Exp l = n.left, r = n.right;
	switch (n.binop)
	{
	case BINOP.PLUS:
	    if (fits12(r)) return immediate("addi", l, value(r));
	    if (fits12(l)) return immediate("addi", r, value(l));
	    return register("add", l, r);

	case BINOP.MINUS:
	    if (fits12(r) && value(r) != -2048)
		return immediate("addi", l, -value(r));
	    return register("sub", l, r);

	case BINOP.MUL:
	    if (isPow2(r)) return immediate("sll", l, log2(r));
	    if (isPow2(l)) return immediate("sll", r, log2(l));
	    return register("mul", l, r);

	case BINOP.DIV:
	    if (isPow2(r)) return immediate("sra", l, log2(r));
	    return register("div", l, r);

	case BINOP.AND:
	case BINOP.BITAND:
	    if (fits12(r)) return immediate("andi", l, value(r));
	    if (fits12(l)) return immediate("andi", r, value(l));
	    return register("and", l, r);

	case BINOP.OR:
	case BINOP.BITOR:
	    if (fits12(r)) return immediate("ori", l, value(r));
	    if (fits12(l)) return immediate("ori", r, value(l));
	    return register("or", l, r);

	case BINOP.BITXOR:
	    if (fits12(r)) return immediate("xori", l, value(r));
	    if (fits12(l)) return immediate("xori", r, value(l));
	    return register("xor", l, r);

	case BINOP.LSHIFT:
	    if (fits12(r)) return immediate("sll", l, value(r));
	    return register("sll", l, r);

	case BINOP.RSHIFT:
	    if (fits12(r)) return immediate("srl", l, value(r));
	    return register("srl", l, r);

	case BINOP.ARSHIFT:
	    if (fits12(r)) return immediate("sra", l, value(r));
	    return register("sra", l, r);
	}
	throw new Error("bad binop " + n.binop);
    }

    /**
     * Calls: the first eight arguments are moved into a0..a7, the rest are
     * stored on the stack.  A call clobbers every caller-save register, so
     * those are its defs; its uses are the argument registers.  The result
     * is in a0.
     */
    public Temp visit(CALL n)
    {
	Temp f = null;
	if (!(n.func instanceof NAME))
	    f = munch(n.func);

	LinkedList<Temp> uses = new LinkedList<Temp>();
	if (f != null)
	    uses.add(f);

	int i = 0;
	for (Tree.Exp arg : n.args)
	{
	    Temp a = munch(arg);
	    if (i < RiscvFrame.argRegs.length)
	    {
		emit(new MOVE("mv `d0, `s0", RiscvFrame.argRegs[i], a));
		uses.add(RiscvFrame.argRegs[i]);
	    }
	    else
	    {
		int offset = (i - RiscvFrame.argRegs.length) * frame.wordSize();
		emit(new OPER("sw `s0, " + offset + "(`s1)", null,
			      L(a, RiscvFrame.SP), null));
	    }
	    i++;
	}

	Temp[] used = uses.toArray(new Temp[0]);
	if (n.func instanceof NAME)
	    emit(new OPER("call " + ((NAME) n.func).label,
			  RiscvFrame.calldefs.clone(), used, null));
	else
	    emit(new OPER("jalr `s0", RiscvFrame.calldefs.clone(), used, null));
	return frame.RV();
    }

    public Temp visit(ESEQ n)
    {
	throw new Error("ESEQ cannot appear in canonical trees");
    }
}
