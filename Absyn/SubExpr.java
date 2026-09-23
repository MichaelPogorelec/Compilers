/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Subtraction Expressions.
 */
public class SubExpr extends BinOpExpr
{
    public SubExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
}
