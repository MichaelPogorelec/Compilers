package Absyn;

/**
 * Subtraction Expressions.
 */
public class SubExpr extends BinOpExpr
{
    public SubExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
