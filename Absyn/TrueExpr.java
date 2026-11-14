package Absyn;

/**
 * Boolean TRUE.
 */
public class TrueExpr extends Expr
{
    public TrueExpr() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
