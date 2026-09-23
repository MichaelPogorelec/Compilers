/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Division Expressions.
 */
public class DivExpr extends BinOpExpr
{
    public DivExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
}
