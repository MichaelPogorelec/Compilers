package Absyn;

/**
 * The NULL expression.
 */
public class NullExpr extends Expr
{
    public NullExpr() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
