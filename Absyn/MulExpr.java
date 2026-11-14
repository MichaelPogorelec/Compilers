package Absyn;

/**
 * Multiplication Expressions.
 */
public class MulExpr extends BinOpExpr
{
    public MulExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
