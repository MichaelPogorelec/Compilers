/* Copyright (C) 2007, Marquette University.  All rights reserved. */
package Absyn;

/**
 * Parent class of all abstract syntax tree nodes.
 */

public abstract class Absyn implements Visitable
{
    /** Visitor pattern dispatch. */
    public abstract void accept(Visitor v);

    /** Visitor pattern dispatch for the translator (Project 7). */
    public abstract Translate.Exp accept(TreeVisitor v);
}
