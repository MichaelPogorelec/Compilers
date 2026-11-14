package Absyn;

/**
 * Boolean Not Expressions.
 */
public class NotExpr extends Expr
{
    public Expr operand;

    public NotExpr(Expr operand) { this.operand = operand; }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
