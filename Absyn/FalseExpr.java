package Absyn;

/**
 * Boolean FALSE.
 */
public class FalseExpr extends Expr
{
    public FalseExpr() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
