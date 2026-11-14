package Absyn;

/**
 * The "this" reference.
 */
public class ThisExpr extends Expr
{
    public ThisExpr() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
