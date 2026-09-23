/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Greater-Than Comparison Expressions.
 */
public class GreaterExpr extends BinOpExpr
{
    public GreaterExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
}
