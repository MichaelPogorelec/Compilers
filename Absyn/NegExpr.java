/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Unary Negation Expressions.
 */
public class NegExpr extends Expr
{
    public Expr operand;

    public NegExpr(Expr operand) { this.operand = operand; }

    public void accept(Visitor v) { v.visit(this); }
}
