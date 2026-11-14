package Absyn;

/**
 * Unary Negation Expressions.
 */
public class NegExpr extends Expr
{
    public Expr operand;

    public NegExpr(Expr operand) { this.operand = operand; }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
