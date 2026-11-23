/**
 * COSC 4400 - Project 8
 * RISC-V instruction selector: tiles canonical Tree statements with RISC-V
 * instructions (RiscvPatterns.txt), using an unlimited supply of temps.
 * Expression nodes return the temp that holds their value.
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
	    emit(new OPER("\tj\t`j0", null, null, n.targets));
	else
	    emit(new OPER("\tjalr\t`s0", null, L(munch(n.exp)), n.targets));
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
	emit(new OPER("\t" + branch(n.relop) + "\t`s0,\t`s1,\t`j0", null, L(l, r),
		      targets));
    }

    /**
     * Stores take the value first and then the address.  The temps of a
     * pattern's operands are made in the order the operands are munched.
     */
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
		    Temp v = munch(n.src);
		    Temp a = munch(b.left);
		    emit(new OPER("\tsw\t`s0,\t" + value(b.right) + "(`s1)",
				  null, L(v, a), null));
		    return;
		}
		if (fits12(b.left))
		{
		    Temp v = munch(n.src);
		    Temp a = munch(b.right);
		    emit(new OPER("\tsw\t`s0,\t" + value(b.left) + "(`s1)",
				  null, L(v, a), null));
		    return;
		}
	    }
	    Temp v = munch(n.src);
	    Temp a = munch(addr);
	    emit(new OPER("\tsw\t`s0,\t(`s1)", null, L(v, a), null));
	}
	else if (n.dst instanceof TEMP)
	{
	    Temp s = munch(n.src);
	    emit(new MOVE("\tmv\t`d0,\t`s0", ((TEMP) n.dst).temp, s));
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
	emit(new OPER("\tli\t`d0,\t" + n.value, L(d), null, null));
	return d;
    }

    public Temp visit(NAME n)
    {
	Temp d = new Temp();
	emit(new OPER("\tla\t`d0,\t" + n.label, L(d), null, null));
	return d;
    }

    /**
     * The result temp is made before the operand is munched, so an outer
     * load has a lower number than the loads inside its address.
     */
    public Temp visit(MEM n)
    {
	Tree.Exp addr = n.exp;
	Temp d = new Temp();
	if (addr instanceof BINOP && ((BINOP) addr).binop == BINOP.PLUS)
	{
	    BINOP b = (BINOP) addr;
	    if (fits12(b.left))
	    {
		Temp s = munch(b.right);
		emit(new OPER("\tlw\t`d0,\t" + value(b.left) + "(`s0)",
			      L(d), L(s), null));
		return d;
	    }
	    if (fits12(b.right))
	    {
		Temp s = munch(b.left);
		emit(new OPER("\tlw\t`d0,\t" + value(b.right) + "(`s0)",
			      L(d), L(s), null));
		return d;
	    }
	}
	Temp s = munch(addr);
	emit(new OPER("\tlw\t`d0,\t(`s0) // this guy", L(d), L(s), null));
	return d;
    }

    /** op Rd, Rs, imm */
    private Temp immediate(Temp d, String op, Tree.Exp operand, int imm)
    {
	Temp s = munch(operand);
	emit(new OPER("\t" + op + "\t`d0,\t`s0,\t" + imm, L(d), L(s), null));
	return d;
    }

    /** op Rd, Rs1, Rs2 */
    private Temp register(Temp d, String op, Tree.Exp left, Tree.Exp right)
    {
	Temp l = munch(left);
	Temp r = munch(right);
	emit(new OPER("\t" + op + "\t`d0,\t`s0,\t`s1", L(d), L(l, r), null));
	return d;
    }

    public Temp visit(BINOP n)
    {
	Tree.Exp l = n.left, r = n.right;
	// The result temp is made first (and is simply unused when the
	// pattern needs no instruction).
	Temp d = new Temp();
	switch (n.binop)
	{
	case BINOP.PLUS:
	    if (fits12(r) && value(r) == 0) return munch(l);
	    if (fits12(l) && value(l) == 0) return munch(r);
	    if (fits12(r)) return immediate(d, "addi", l, value(r));
	    if (fits12(l)) return immediate(d, "addi", r, value(l));
	    return register(d, "add", l, r);

	case BINOP.MINUS:
	    if (fits12(r) && value(r) != -2048)
		return immediate(d, "addi", l, -value(r));
	    return register(d, "sub", l, r);

	case BINOP.MUL:
	    if (isPow2(r)) return immediate(d, "sll", l, log2(r));
	    if (isPow2(l)) return immediate(d, "sll", r, log2(l));
	    return register(d, "mul", l, r);

	case BINOP.DIV:
	    if (isPow2(r)) return immediate(d, "sra", l, log2(r));
	    return register(d, "div", l, r);

	case BINOP.AND:
	case BINOP.BITAND:
	    if (fits12(r)) return immediate(d, "andi", l, value(r));
	    if (fits12(l)) return immediate(d, "andi", r, value(l));
	    return register(d, "and", l, r);

	case BINOP.OR:
	case BINOP.BITOR:
	    if (fits12(r)) return immediate(d, "ori", l, value(r));
	    if (fits12(l)) return immediate(d, "ori", r, value(l));
	    return register(d, "or", l, r);

	case BINOP.BITXOR:
	    if (fits12(r)) return immediate(d, "xori", l, value(r));
	    if (fits12(l)) return immediate(d, "xori", r, value(l));
	    return register(d, "xor", l, r);

	case BINOP.LSHIFT:
	    if (fits12(r)) return immediate(d, "sll", l, value(r));
	    return register(d, "sll", l, r);

	case BINOP.RSHIFT:
	    if (fits12(r)) return immediate(d, "srl", l, value(r));
	    return register(d, "srl", l, r);

	case BINOP.ARSHIFT:
	    if (fits12(r)) return immediate(d, "sra", l, value(r));
	    return register(d, "sra", l, r);
	}
	throw new Error("bad binop " + n.binop);
    }

    /**
     * Calls: the arguments are munched and moved into a0..a7 (the rest are
     * stored on the stack) first; for an indirect call the function
     * expression is munched after them.  A call clobbers every caller-save
     * register, so those are its defs; its uses are the function (if any)
     * and the argument registers.  A "Call sink" pseudo-instruction that
     * uses the clobbered registers follows, and the result is in a0.
     */
    public Temp visit(CALL n)
    {
	LinkedList<Temp> argUses = new LinkedList<Temp>();
	int i = 0;
	for (Tree.Exp arg : n.args)
	{
	    Temp a = munch(arg);
	    if (i < RiscvFrame.argRegs.length)
	    {
		emit(new MOVE("\tmv\t`d0,\t`s0", RiscvFrame.argRegs[i], a));
		argUses.add(RiscvFrame.argRegs[i]);
	    }
	    else
	    {
		int offset = (i - RiscvFrame.argRegs.length) * frame.wordSize();
		emit(new OPER("\tsw\t`s0,\t" + offset + "(`s1)", null,
			      L(a, RiscvFrame.SP), null));
	    }
	    i++;
	}

	if (n.func instanceof NAME)
	{
	    emit(new OPER("\tcall\t" + ((NAME) n.func).label,
			  RiscvFrame.calldefs.clone(),
			  argUses.toArray(new Temp[0]), null));
	}
	else
	{
	    LinkedList<Temp> uses = new LinkedList<Temp>();
	    uses.add(munch(n.func));
	    uses.addAll(argUses);
	    emit(new OPER("\tjalr\t`s0", RiscvFrame.calldefs.clone(),
			  uses.toArray(new Temp[0]), null));
	}
	emit(new OPER("\t// Call sink", null, RiscvFrame.calldefs.clone(),
		      null));
	return frame.RV();
    }

    public Temp visit(ESEQ n)
    {
	throw new Error("ESEQ cannot appear in canonical trees");
    }
}
