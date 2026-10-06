package Absyn;

/**
 * Boolean Not Expressions.
 */
public class NotExpr extends Expr
{
    public Expr operand;

    public NotExpr(Expr operand) { this.operand = operand; }

    public void accept(Visitor v) { v.visit(this); }
}
