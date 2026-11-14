package Absyn;

/**
 * Boolean And Expressions.
 */
public class AndExpr extends BinOpExpr
{
    public AndExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
