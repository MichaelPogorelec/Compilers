/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Boolean (Logical) Or Expressions.
 */
public class OrExpr extends BinOpExpr
{
    public OrExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
}
