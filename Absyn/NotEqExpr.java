package Absyn;

/**
 * Boolean Not Equal Expressions.
 */

public class NotEqExpr extends BinOpExpr
{
    public NotEqExpr(Expr e1, Expr e2) { super(e1, e2); }

    /** Wisitor Pattern dispatch. */
    public void accept(Visitor v)
    {
        v.visit(this);
    }
}
