package Absyn;

/**
 * Boolean Or Expressions.
 */
public class OrExpr extends BinOpExpr
{
    public OrExpr(Expr e1, Expr e2) { super(e1, e2); }

    public void accept(Visitor v) { v.visit(this); }
}
