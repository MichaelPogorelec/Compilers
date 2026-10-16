/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Absyn;
import java.io.PrintWriter;

/**
 * Visitor prints AST in reparseable form.
 */

public class PrintVisitor implements Visitor
{
    PrintWriter out;
    public int indentCount = 0;

    public PrintVisitor(PrintWriter out)
    {
		this.out = out;
    }

    public PrintVisitor()
    {
		this.out = new PrintWriter(System.out);
    }

    private void indent()
    {
		out.print('\n');
		for(int i = 0; i < indentCount; i++)
	    { out.print(' '); }
    }

    /** Visitor pattern dispatch. */
    //public void visit(Absyn ast) {}

    public void visit(Program ast)
    {
		out.print("Program(");
		indentCount++;
		visit(ast.classes);
		indentCount--;
		out.println(")");
		out.flush();
    }

    public void visit(java.util.AbstractList list)
    {
		if (null == list)
	    {
			indent();
			out.print("null");
			return;
	    }
		indent();
		out.print("AbstractList(");
		indentCount++;
		for (Object o : list)
	    {
			if (null == o)
		    {   indent();   out.print("null");   }
			else
		    {   ((Visitable)o).accept(this);    }	
	    }
		out.print(")");
		indentCount--;
    }

    public void visit(ClassDecl ast)
    {
		indent();
		out.print("ClassDecl(");
		indentCount++;
		out.print(ast.name + " " + ast.parent); 
		visit(ast.fields);
		visit(ast.methods);
		printClassDesc(ast.descriptor);
		indentCount--;
		out.print(")");
    }

    public void visit(MethodDecl ast)
    {
		indent();
		out.print("MethodDecl(");
		indentCount++;
		if (null != ast.returnType)
	    { ast.returnType.accept(this); }
		else
	    { out.print("public_static_void"); }
		if (ast.synced) { out.print(" synchronized"); }
		out.print(" " + ast.name);
		visit(ast.params);
        visit(ast.locals);
		visit(ast.stmts);
		ast.returnVal.accept(this);
		printTypeDesc(ast.descriptor);
		indentCount--;
		out.print(")");
    }
    
    public void visit(Formal ast)
    {
		indent();
		out.print("Formal(");
		ast.type.accept(this);
		out.print(" " + ast.name + ")");
    }

    public void visit(IdentifierType ast)
    {
		out.print("IdentifierType(" + ast.id + ")");
    }

    public void visit(VarDecl ast)
    {
		indent();
		out.print("VarDecl(");
		ast.type.accept(this);
		out.print(" " + ast.name);
		if (null == ast.resolved)
	    {
			// Not yet annotated (e.g. shown in an error message).
			if (null == ast.init)
		    {   out.print(" null");   }
			else
		    {   ast.init.accept(this);   }
			out.print(")");
			return;
	    }
		indentCount++;
		if (null == ast.init)
	    {   out.print(" null");   } 
		else
	    { ast.init.accept(this); }
		out.print(" ");
		printTypeDesc(ast.resolved);
		indentCount--;
		out.print(")");
    }

    public void visit(XinuCallStmt ast)
    {
		indent();
		out.print("XinuCallStmt(");
		indentCount++;
		out.print(ast.method);
		visit(ast.args);
		indentCount--;
		out.print(")");
    }

    public void visit(IntegerLiteral ast)
    {
		indent();
		out.print("IntegerLiteral(" + ast.value + ")");
    }

    public void visit(StringLiteral ast)
    {
		indent();
		out.print("StringLiteral(" + ast.value + ")");
    }

    public void visit(ArrayType ast)
    {
		out.print("ArrayType(");
		ast.base.accept(this);
		out.print(")");
    }

    /* ---- Types ------------------------------------------------ */
    /* Types print inline, with no indent() call, like IdentifierType. */

    public void visit(IntegerType ast)
    {
                out.print("IntegerType");
    }

    public void visit(BooleanType ast)
    {
                out.print("BooleanType");
    }

    /* ---- Declarations ----------------------------------------- */

    public void visit(ThreadDecl ast)
    {
                indent();
                out.print("ThreadDecl(");
                indentCount++;
                out.print(ast.name);
                visit(ast.fields);
                visit(ast.methods);
                printClassDesc(ast.descriptor);
                indentCount--;
                out.print(")");
    }

    public void visit(VoidDecl ast)
    {
                indent();
                out.print("VoidDecl(");
                indentCount++;
                out.print(ast.name);
                visit(ast.locals);
                visit(ast.stmts);
                printTypeDesc(ast.descriptor);
                indentCount--;
                out.print(")");
    }

    /* ---- Statements ------------------------------------------- */

    public void visit(BlockStmt ast)
    {
                indent();
                out.print("BlockStmt(");
                indentCount++;
                visit(ast.stmts);
                indentCount--;
                out.print(")");
    }

    public void visit(AssignStmt ast)
    {
                indent();
                out.print("AssignStmt(");
                indentCount++;
                ast.lhs.accept(this);
                ast.rhs.accept(this);
                indentCount--;
                out.print(")");
    }

    public void visit(IfStmt ast)
    {
                indent();
                out.print("IfStmt(");
                indentCount++;
                ast.test.accept(this);
                ast.thenStmt.accept(this);
                if (null == ast.elseStmt)
            {   indent();   out.print("null");   }
                else
            {   ast.elseStmt.accept(this);   }
                indentCount--;
                out.print(")");
    }

    public void visit(WhileStmt ast)
    {
                indent();
                out.print("WhileStmt(");
                indentCount++;
                ast.test.accept(this);
                ast.body.accept(this);
                indentCount--;
                out.print(")");
    }

    /* ---- Binary operators ------------------------------------- */
    /* One method per operator; all of them print their two operands. */

    public void visit(AddExpr ast)     { binop("AddExpr", ast); }
    public void visit(SubExpr ast)     { binop("SubExpr", ast); }
    public void visit(MulExpr ast)     { binop("MulExpr", ast); }
    public void visit(DivExpr ast)     { binop("DivExpr", ast); }
    public void visit(AndExpr ast)     { binop("AndExpr", ast); }
    public void visit(OrExpr ast)      { binop("OrExpr", ast); }
    public void visit(EqualExpr ast)   { binop("EqualExpr", ast); }
    public void visit(NotEqExpr ast)   { binop("NotEqExpr", ast); }
    public void visit(LesserExpr ast)  { binop("LesserExpr", ast); }
    public void visit(GreaterExpr ast) { binop("GreaterExpr", ast); }

    private void binop(String name, BinOpExpr ast)
    {
                indent();
                out.print(name + "(");
                indentCount++;
                ast.e1.accept(this);
                ast.e2.accept(this);
                indentCount--;
                out.print(")");
    }

    /* ---- Unary operators -------------------------------------- */

    public void visit(NegExpr ast)
    {
                indent();
                out.print("NegExpr(");
                indentCount++;
                ast.operand.accept(this);
                indentCount--;
                out.print(")");
    }

    public void visit(NotExpr ast)
    {
                indent();
                out.print("NotExpr(");
                indentCount++;
                ast.operand.accept(this);
                indentCount--;
                out.print(")");
    }

    /* ---- Constants -------------------------------------------- */

    public void visit(TrueExpr ast)
    {
                indent();
                out.print("TrueExpr");
    }

    public void visit(FalseExpr ast)
    {
                indent();
                out.print("FalseExpr");
    }

    public void visit(ThisExpr ast)
    {
                indent();
                out.print("ThisExpr");

    }

    public void visit(NullExpr ast)
    {
                indent();
                out.print("NullExpr");
    }

    /* ---- References ------------------------------------------- */

    public void visit(IdentifierExpr ast)
    {
                indent();
                out.print("IdentifierExpr(" + ast.id + ")");

    }

    public void visit(FieldExpr ast)
    {
                indent();
                out.print("FieldExpr(");
                indentCount++;
                ast.object.accept(this);
                indent();
                out.print(ast.name);
                if (null != ast.index)
                {
                        indent();
                        out.print(ast.index);
                }
                indentCount--;
                out.print(")");
    }

    public void visit(ArrayExpr ast)
    {
                indent();
                out.print("ArrayExpr(");
                indentCount++;
                ast.array.accept(this);
                ast.index.accept(this);
                indentCount--;
                out.print(")");
    }

    /* ---- Calls ------------------------------------------------ */

    public void visit(CallExpr ast)
    {
                indent();
                out.print("CallExpr(");
                indentCount++;
                ast.object.accept(this);
                indent();
                out.print(ast.method);
                visit(ast.args);
                if (null != ast.index)
                {
                        indent();
                        out.print(ast.index);
                }
                indentCount--;
                out.print(")");
    }

    public void visit(XinuCallExpr ast)
    {
                indent();
                out.print("XinuCallExpr(");
                indentCount++;
                out.print(ast.method);
                visit(ast.args);
                indentCount--;
                out.print(")");
    }

    /* ---- Allocation ------------------------------------------- */

    public void visit(NewObjectExpr ast)
    {
                indent();
                out.print("NewObjectExpr(");
                ast.type.accept(this);
                out.print(")");
    }

    public void visit(NewArrayExpr ast)
    {
                indent();
                out.print("NewArrayExpr(");
                indentCount++;
                ast.type.accept(this);
                visit(ast.dims);
                indentCount--;
                out.print(")");
    }
    /* ---- Type descriptors (the checker's annotations) ------------------
     * Same layout as the descriptor dump, but every line starts with
     * indent() so a descriptor can nest inside the AST dump.            */

    private String simpleType(Types.Type t)
    {
                if (t instanceof Types.OBJECT)
            { return "OBJECT(" + ((Types.OBJECT) t).myClass.name + ")"; }
                if (t instanceof Types.INT)      { return "INT"; }
                if (t instanceof Types.BOOLEAN)  { return "BOOLEAN"; }
                if (t instanceof Types.VOID)     { return "VOID"; }
                if (t instanceof Types.NIL)      { return "NIL"; }
                return (null == t) ? "null" : t.toString();
    }

    private void printClassDesc(Types.CLASS c)
    {
                indent();
                out.print("CLASS(" + c.name);
                indentCount++;
                indent();
                out.print(null == c.parent ? "null" : c.parent.name);
                printRecordDesc(c.methods);
                printRecordDesc(c.fields);
                indent();
                out.print("OBJECT(" + c.instance.myClass.name);
                indentCount++;
                printRecordDesc(c.instance.methods);
                printRecordDesc(c.instance.fields);
                indentCount--;
                out.print(")");
                indentCount--;
                out.print(")");
    }

    private void printRecordDesc(Types.RECORD r)
    {
                indent();
                out.print("RECORD(");
                indentCount++;
                for (Types.FIELD f : r)
            {
                        indent();
                        out.print("FIELD(" + f.index + " " + f.name);
                        indentCount++;
                        printTypeDesc(f.type);
                        indentCount--;
                        out.print(")");
            }
                indentCount--;
                out.print(")");
    }

    private void printTypeDesc(Types.Type t)
    {
                indent();
                if (t instanceof Types.FUNCTION)
            {
                        Types.FUNCTION fn = (Types.FUNCTION) t;
                        out.print("FUNCTION(" + fn.name);
                        indentCount++;
                        indent();
                        out.print(simpleType(fn.self));
                        printRecordDesc(fn.formals);
                        printTypeDesc(fn.result);
                        indentCount--;
                        out.print(")");
            }
                else if (t instanceof Types.ARRAY)
            {
                        out.print("ARRAY(");
                        indentCount++;
                        printTypeDesc(((Types.ARRAY) t).element);
                        indentCount--;
                        out.print(")");
            }
                else
            { out.print(simpleType(t)); }
    }
}
