/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Boolean (Logical) Not Expressions.
 */
public class NotExpr extends Expr
{
    public Expr operand;

    public NotExpr(Expr operand) { this.operand = operand; }

    public void accept(Visitor v) { v.visit(this); }
}
