/**
 * COSC 4400 - Project 7
 * Translates the type-checked MiniJava AST into Tree intermediate code
 * (Appel chapters 6 and 7).
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Translate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import Symbol.Symbol;
import Temp.Label;
import Temp.Temp;
import Types.CLASS;
import Types.FIELD;

/**
 * Implements translation of abstract syntax expressions
 * into intermediate trees.
 *
 * Run-time layout used by the generated code (one word = frame.wordSize()),
 * matching the reference translator:
 *
 *   object : the pointer addresses field 0; field i is at byte offset
 *            i * wordSize (FieldExpr.index); the class's vtable pointer is
 *            the word just before the object (offset -wordSize)
 *   vtable : slot k, at byte offset k * wordSize, holds the entry label of
 *            the method with index k (CallExpr.index); an overriding
 *            method reuses the slot of the method it overrides
 *   array  : element i is at byte offset i * wordSize and the length is
 *            the word just before the elements, so it is "field" -1
 *            (FieldExpr.index == -1)
 *
 * Methods take the receiver ("this") as their first argument, main
 * included.  Null pointers and out-of-range subscripts jump to
 * frame.badPtr() and frame.badSub().  Storage comes from the runtime
 * routine _new (objects: _new(numFields, vtable); arrays: _new(n, n));
 * Xinu calls become calls to _print, _printint, etc.  Every local is
 * zeroed on entry, and every method ends by moving its result (0 for
 * main and void methods) into the return-value register.
 */

public class Translate implements Absyn.TreeVisitor
{
    /** Frame factory; the frame of each method comes from newFrame(). */
    public Frame.Frame frame;

    /** Bytes per word on the target. */
    private final int ws;

    public Translate(Frame.Frame frame)
    {
	this.frame = frame;
	this.ws = frame.wordSize();
    }

    /* ---------------------------------------------------------------- */
    /* Tree builders                                                    */
    /* ---------------------------------------------------------------- */

    private static Tree.Exp CONST(int value)  { return new Tree.CONST(value); }
    private static Tree.Exp NAME(Label label) { return new Tree.NAME(label); }
    private static Tree.Exp TEMP(Temp temp)   { return new Tree.TEMP(temp); }
    private static Tree.Exp BINOP(int binop, Tree.Exp left, Tree.Exp right)
    {   return new Tree.BINOP(binop, left, right);   }
    private static Tree.Exp MEM(Tree.Exp exp) { return new Tree.MEM(exp); }
    private static Tree.Exp CALL(Tree.Exp func, List<Tree.Exp> args)
    {   return new Tree.CALL(func, args);   }
    private static Tree.Exp ESEQ(Tree.Stm stm, Tree.Exp exp)
    {
	if (stm == null) return exp;
	return new Tree.ESEQ(stm, exp);
    }

    private static Tree.Stm MOVE(Tree.Exp dst, Tree.Exp src)
    {
	return new Tree.MOVE(dst, src);
    }
    private static Tree.Stm EXP(Tree.Exp exp) { return new Tree.EXP(exp); }
    private static Tree.Stm JUMP(Label target)
    {   return new Tree.JUMP(target);   }
    private static Tree.Stm CJUMP(int relop, Tree.Exp l, Tree.Exp r,
				  Label t, Label f)
    {   return new Tree.CJUMP(relop, l, r, t, f);   }
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

    /** A one-element argument list. */
    private static LinkedList<Tree.Exp> args(Tree.Exp... exps)
    {
	LinkedList<Tree.Exp> list = new LinkedList<Tree.Exp>();
	for (Tree.Exp e : exps)
	    list.add(e);
	return list;
    }

    /** Call to a routine of the run-time library. */
    private Tree.Exp external(String name, List<Tree.Exp> arguments)
    {
	return frame.externalCall(Symbol.symbol(name), arguments);
    }

    /** exp + bytes; the reference keeps the BINOP even for offset 0. */
    private static Tree.Exp offset(Tree.Exp exp, int bytes)
    {
	return BINOP(Tree.BINOP.PLUS, exp, CONST(bytes));
    }

    /** A TEMP or CONST is cheap and side-effect free: safe to repeat. */
    private static boolean isSimple(Tree.Exp exp)
    {
	return (exp instanceof Tree.TEMP) || (exp instanceof Tree.CONST);
    }

    /** A fresh node for a simple expression, so subtrees are not shared. */
    private static Tree.Exp copy(Tree.Exp exp)
    {
	if (exp instanceof Tree.TEMP)
	    return new Tree.TEMP(((Tree.TEMP) exp).temp);
	if (exp instanceof Tree.CONST)
	    return new Tree.CONST(((Tree.CONST) exp).value);
	return exp;
    }

    /** Statement that jumps to _BADPTR when p is null. */
    private Tree.Stm nullCheck(Tree.Exp p)
    {
	Label ok = new Label();
	return SEQ(CJUMP(Tree.CJUMP.EQ, p, CONST(0), frame.badPtr(), ok),
		   LABEL(ok));
    }

    /* ---------------------------------------------------------------- */
    /* Translation state                                                */
    /* ---------------------------------------------------------------- */

    private LinkedList<Frag> frags = new LinkedList<Frag>();
    public List<Frag> results() { return frags; }

    /** Class descriptors, from the type checker, by class name. */
    private HashMap<String, CLASS>  classEnv  = new HashMap<String, CLASS>();
    /** Entry label of each method, by "Class$method". */
    private HashMap<String, Label>  methodEnv = new HashMap<String, Label>();
    /** Label of each class's vtable, by class name. */
    private HashMap<String, Label>  vtableEnv = new HashMap<String, Label>();
    /** Formals and locals of the method being translated. */
    private HashMap<String, Frame.Access> varEnv =
	new HashMap<String, Frame.Access>();

    /** Class and method being translated. */
    private CLASS currentClass;
    private Frame.Frame cur;
    private Frame.Access thisAccess;

    private static boolean isMain(Absyn.MethodDecl m)
    {
	return m.returnType == null && !(m instanceof Absyn.VoidDecl);
    }

    private static String methodLabelName(String className, Absyn.MethodDecl m)
    {
	return className + "__" + m.name;
    }

    /** The receiver of the current (non-static) method. */
    private Tree.Exp thisExp()
    {
	return thisAccess.exp(cur);
    }

    /* ---------------------------------------------------------------- */
    /* Declarations                                                     */
    /* ---------------------------------------------------------------- */

    public Exp visit(Absyn.Program n)
    {
	// Labels first, so any method can call any other.
	for (Absyn.ClassDecl c : n.classes)
	{
	    classEnv.put(c.name, c.descriptor);
	    vtableEnv.put(c.name, new Label(c.name + "__vtable"));
	    for (Absyn.MethodDecl m : c.methods)
		methodEnv.put(c.name + "__" + m.name,
			      new Label(methodLabelName(c.name, m)));
	}
	for (Absyn.ClassDecl c : n.classes)
	    emitVtable(c);
	for (Absyn.ClassDecl c : n.classes)
	    c.accept(this);
	return new Nx(null);
    }

    /** The vtable holds, in method-index order, each method's entry label. */
    private void emitVtable(Absyn.ClassDecl c)
    {
	LinkedList<Label> entries = new LinkedList<Label>();
	for (FIELD method : c.descriptor.instance.methods)
	{
	    // The implementation is in the nearest class, going up the
	    // parents, that declares a method of this name.
	    CLASS owner = c.descriptor;
	    while (owner != null && owner.methods.get(method.name) == null)
		owner = owner.parent;
	    entries.add(methodEnv.get(owner.name + "__" + method.name));
	}
	frags.add(new DataFrag(frame.record(vtableEnv.get(c.name), entries)));
    }

    public Exp visit(Absyn.ClassDecl c)
    {
	currentClass = c.descriptor;
	for (Absyn.MethodDecl m : c.methods)
	    m.accept(this);
	currentClass = null;
	return new Nx(null);
    }

    public Exp visit(Absyn.ThreadDecl c)
    {
	return visit((Absyn.ClassDecl) c);
    }

    public Exp visit(Absyn.MethodDecl m)
    {
	translateMethod(m);
	return new Nx(null);
    }

    public Exp visit(Absyn.VoidDecl m)
    {
	translateMethod(m);
	return new Nx(null);
    }

    /**
     * One procedure fragment per method: a new frame, the formals ("this"
     * first), the locals and their initializers, the statements, and for
     * a method with a result, a move of the return value into RV.
     */
    private void translateMethod(Absyn.MethodDecl m)
    {
	boolean main = isMain(m);
	Frame.Frame savedFrame = cur;
	Frame.Access savedThis = thisAccess;
	HashMap<String, Frame.Access> savedVars = varEnv;

	cur = frame.newFrame(Symbol.symbol(main ? "main"
					  : methodLabelName(currentClass.name, m)));
	varEnv = new HashMap<String, Frame.Access>();
	thisAccess = cur.allocFormal();

	for (Absyn.Formal f : m.params)
	    f.accept(this);
	// Allocate every local before translating any initializer, since
	// an initializer may mention a local declared after it.
	for (Absyn.VarDecl v : m.locals)
	    varEnv.put(v.name, cur.allocLocal());

	Tree.Stm body = null;
	for (Absyn.VarDecl v : m.locals)
	    body = SEQ(body, v.accept(this).unNx());
	for (Absyn.Stmt s : m.stmts)
	    body = SEQ(body, s.accept(this).unNx());
	// main and void methods return 0.
	Tree.Exp result = (m.returnVal == null)
	    ? CONST(0) : m.returnVal.accept(this).unEx();
	body = SEQ(body, MOVE(TEMP(cur.RV()), result));

	frags.add(new ProcFrag(body, cur));

	cur = savedFrame;
	thisAccess = savedThis;
	varEnv = savedVars;
    }

    public Exp visit(Absyn.Formal f)
    {
	varEnv.put(f.name, cur.allocFormal());
	return new Nx(null);
    }

    /** A local with an initializer becomes a move; otherwise nothing. */
    public Exp visit(Absyn.VarDecl v)
    {
	Frame.Access access = varEnv.get(v.name);
	if (v.init == null)
	    return new Nx(MOVE(access.exp(cur), CONST(0)));
	return new Nx(MOVE(access.exp(cur), v.init.accept(this).unEx()));
    }

    public Exp visit(Absyn.BooleanType  t)   { return new Nx(null); }
    public Exp visit(Absyn.IntegerType  t)   { return new Nx(null); }
    public Exp visit(Absyn.ArrayType t)      { return new Nx(null); }
    public Exp visit(Absyn.IdentifierType t) { return new Nx(null); }

    /* ---------------------------------------------------------------- */
    /* Statements                                                       */
    /* ---------------------------------------------------------------- */

    public Exp visit(Absyn.AssignStmt s)
    {
	Tree.Exp lhs = s.lhs.accept(this).unEx();
	Tree.Exp rhs = s.rhs.accept(this).unEx();
	return new Nx(MOVE(lhs, rhs));
    }

    public Exp visit(Absyn.BlockStmt b)
    {
	Tree.Stm result = null;
	for (Absyn.Stmt s : b.stmts)
	    result = SEQ(result, s.accept(this).unNx());
	return new Nx(result);
    }

    public Exp visit(Absyn.IfStmt s)
    {
	Exp test = s.test.accept(this);
	Exp thenPart = s.thenStmt.accept(this);
	Exp elsePart = (s.elseStmt == null)
	    ? new Nx(null) : s.elseStmt.accept(this);
	return new IfThenElseExp(test, thenPart, elsePart);
    }

    public Exp visit(Absyn.WhileStmt s)
    {
	Exp cond = s.test.accept(this);
	Tree.Stm loopBody = s.body.accept(this).unNx();
	// The reference creates the labels after translating the pieces.
	Label done = new Label();
	Label test = new Label();
	Label body = new Label();
	return new Nx(SEQ(SEQ(SEQ(LABEL(test), cond.unCx(body, done)),
			      SEQ(SEQ(LABEL(body), loopBody), JUMP(test))),
			  LABEL(done)));
    }

    /* ---------------------------------------------------------------- */
    /* Operators                                                        */
    /* ---------------------------------------------------------------- */

    private Exp Ex(int op, Absyn.BinOpExpr e)
    {
        Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new Ex(BINOP(op, e1.unEx(), e2.unEx()));
    }

    public Exp visit(Absyn.AddExpr e) { return Ex(Tree.BINOP.PLUS, e); }
    public Exp visit(Absyn.DivExpr e) { return Ex(Tree.BINOP.DIV, e); }
    public Exp visit(Absyn.MulExpr e) { return Ex(Tree.BINOP.MUL, e); }
    public Exp visit(Absyn.SubExpr e) { return Ex(Tree.BINOP.MINUS, e); }

    public Exp visit(Absyn.NegExpr e)
    {
	return new Ex(BINOP(Tree.BINOP.MINUS,
			    CONST(0), e.operand.accept(this).unEx()));
    }

    public Exp visit(Absyn.AndExpr e)
    {
        Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	// Short-circuit Boolean AND, with false == 0.
	return new IfThenElseExp(e1, e2, new Ex(CONST(0)));
    }

    public Exp visit(Absyn.OrExpr e)
    {
        Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	// Short-circuit Boolean OR, with true == 1.
	return new IfThenElseExp(e1, new Ex(CONST(1)), e2);
    }

    private Exp RelCx(int op, Absyn.BinOpExpr e)
    {
        Exp e1 = e.e1.accept(this);
	Exp e2 = e.e2.accept(this);
	return new RelCx(op, e1.unEx(), e2.unEx());
    }

    public Exp visit(Absyn.GreaterExpr e)  { return RelCx(Tree.CJUMP.GT, e); }
    public Exp visit(Absyn.LesserExpr e)   { return RelCx(Tree.CJUMP.LT, e); }
    public Exp visit(Absyn.EqualExpr e)    { return RelCx(Tree.CJUMP.EQ, e); }
    public Exp visit(Absyn.NotEqExpr e)    { return RelCx(Tree.CJUMP.NE, e); }

    public Exp visit(Absyn.NotExpr e)
    {
        Exp exp = e.operand.accept(this);
        return new IfThenElseExp(exp, new Ex(CONST(0)), new Ex(CONST(1)));
    }

    /* ---------------------------------------------------------------- */
    /* Allocation                                                       */
    /* ---------------------------------------------------------------- */

    /**
     * new C(): allocate a record big enough for the vtable pointer and the
     * fields, then store the class's vtable in word 0.
     */
    public Exp visit(Absyn.NewObjectExpr e)
    {
	String name = ((Absyn.IdentifierType) e.type).id;
	CLASS c = classEnv.get(name);
	return new Ex(external("new", args(CONST(c.instance.fields.size()),
					   NAME(vtableEnv.get(name)))));
    }

    /**
     * new T[n]: the run-time routine _new(n, n) builds the array; the
     * second argument is what _new stores in the word before the block
     * (the vtable for an object, the length for an array).  Trailing
     * empty dimensions only change the type.
     */
    public Exp visit(Absyn.NewArrayExpr e)
    {
	Tree.Exp size = e.dims.getFirst().accept(this).unEx();
	Temp t = new Temp();
	return new Ex(ESEQ(MOVE(TEMP(t), size),
			   external("new", args(TEMP(t), TEMP(t)))));
    }

    /* ---------------------------------------------------------------- */
    /* Calls                                                            */
    /* ---------------------------------------------------------------- */

    private Exp xinuCall(String method, List<Absyn.Expr> arguments)
    {
	LinkedList<Tree.Exp> list = new LinkedList<Tree.Exp>();
	for (Absyn.Expr a : arguments)
	    list.add(a.accept(this).unEx());
	return new Ex(external(method, list));
    }

    public Exp visit(Absyn.XinuCallStmt s)
    {
	return new Nx(EXP(xinuCall(s.method, s.args).unEx()));
    }

    public Exp visit(Absyn.XinuCallExpr e)
    {
	return xinuCall(e.method, e.args);
    }

    /**
     * e.m(a1, ...): fetch the vtable from word 0 of the receiver, the
     * method from slot CallExpr.index, and pass the receiver first.
     */
    public Exp visit(Absyn.CallExpr e)
    {
	// Receiver and arguments are translated first; the temp and label
	// for the receiver null check come after, as in the reference.
	Tree.Exp receiver = e.object.accept(this).unEx();
	LinkedList<Tree.Exp> arguments = new LinkedList<Tree.Exp>();
	for (Absyn.Expr a : e.args)
	    arguments.add(a.accept(this).unEx());

	Temp t = new Temp();
	Tree.Stm prologue = SEQ(MOVE(TEMP(t), receiver), nullCheck(TEMP(t)));
	arguments.addFirst(TEMP(t));

	// The vtable pointer is the word just before the object.
	Tree.Exp vtable = MEM(BINOP(Tree.BINOP.PLUS, TEMP(t), CONST(-ws)));
	Tree.Exp method = MEM(offset(vtable, e.index * ws));
	return new Ex(ESEQ(prologue, CALL(method, arguments)));
    }

    /* ---------------------------------------------------------------- */
    /* Variables, fields and arrays                                     */
    /* ---------------------------------------------------------------- */

    public Exp visit(Absyn.FalseExpr e)
    {   return new Ex(CONST(0));  }

    public Exp visit(Absyn.TrueExpr e)
    {   return new Ex(CONST(1));   }

    public Exp visit(Absyn.IntegerLiteral e)
    {   return new Ex(CONST(e.value));   }

    public Exp visit(Absyn.NullExpr e)
    {   return new Ex(CONST(0));   }

    private HashMap<String, Label> strings = new HashMap<String, Label>();

    /** Strings are static data; the expression is the string's address. */
    public Exp visit(Absyn.StringLiteral e)
    {
	String result = e.value;
	// The scanner keeps the surrounding quotes in the token.
	if (result.length() >= 2 && result.startsWith("\"")
	    && result.endsWith("\""))
	    result = result.substring(1, result.length() - 1);
	result = result.intern();
	Label lab = strings.get(result);
	if (lab != null) return new Ex(NAME(lab));
	lab = new Label();
	strings.put(result, lab);

	DataFrag frag = new DataFrag(frame.string(lab, result));
	frags.add(frag);
	return new Ex(NAME(lab));
    }

    public Exp visit(Absyn.ThisExpr e)
    {
	return new Ex(thisExp());
    }

    /** A name is a local or formal if it is in scope, otherwise a field. */
    public Exp visit(Absyn.IdentifierExpr e)
    {
	Frame.Access access = varEnv.get(e.id);
	if (access != null)
	    return new Ex(access.exp(cur));
	FIELD field = currentClass.instance.fields.get(e.id);
	return fieldAccess(thisExp(), field.index);
    }

    /**
     * Field number index of an object: the object is moved to a temp and
     * null checked, then the field is at byte offset index * wordSize.
     * For array.length the checker records index -1, which is the word
     * just before the elements, where the length is kept.
     */
    private Exp fieldAccess(Tree.Exp object, int index)
    {
	Temp t = new Temp();
	Label ok = new Label();
	Tree.Stm check = SEQ(MOVE(TEMP(t), object),
			     CJUMP(Tree.CJUMP.EQ, TEMP(t), CONST(0),
				   frame.badPtr(), ok));
	return new Ex(ESEQ(check,
			   ESEQ(LABEL(ok),
				MEM(offset(TEMP(t), index * ws)))));
    }

    public Exp visit(Absyn.FieldExpr e)
    {
	return fieldAccess(e.object.accept(this).unEx(), e.index);
    }

    /**
     * a[i]: move a and i to temps, check a is not null, check 0 <= i and
     * i < length (the length is the word before the elements).
     */
    public Exp visit(Absyn.ArrayExpr e)
    {
	Tree.Exp array = e.array.accept(this).unEx();
	Tree.Exp index = e.index.accept(this).unEx();
	Temp a = new Temp();
	Temp i = new Temp();
	Label nonNull = new Label();
	Label nonNeg = new Label();
	Label inRange = new Label();

	Tree.Stm check =
	    SEQ(MOVE(TEMP(a), array),
	    SEQ(MOVE(TEMP(i), index),
	    SEQ(CJUMP(Tree.CJUMP.EQ, TEMP(a), CONST(0),
		      frame.badPtr(), nonNull),
	    SEQ(LABEL(nonNull),
	    SEQ(CJUMP(Tree.CJUMP.LT, TEMP(i), CONST(0),
		      frame.badSub(), nonNeg),
	    SEQ(LABEL(nonNeg),
		CJUMP(Tree.CJUMP.GE, TEMP(i),
		      MEM(BINOP(Tree.BINOP.PLUS, TEMP(a), CONST(-ws))),
		      frame.badSub(), inRange)))))));
	Tree.Exp element =
	    MEM(BINOP(Tree.BINOP.PLUS, TEMP(a),
		      BINOP(Tree.BINOP.MUL, TEMP(i), CONST(ws))));
	return new Ex(ESEQ(check, ESEQ(LABEL(inRange), element)));
    }
}
