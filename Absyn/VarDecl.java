/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Absyn;

/**
 * Declarations for variables and fields.
 */

public class VarDecl extends Absyn
{
    public Type type;
    public String name;
    public Expr init;
    /** Resolved type, filled in by the type checker. */
    public Types.Type resolved;
    public VarDecl(Type type, String name, Expr init)
    {
		this.type = type;
		this.name = name;
		this.init = init;
    }

    /** Visitor pattern dispatch. */
    public void accept(Visitor v) {v.visit(this); }
}
