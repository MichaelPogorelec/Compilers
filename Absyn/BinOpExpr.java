/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Expressions with two operands.
 */
public abstract class BinOpExpr extends Expr
{
    public Expr e1, e2;

    public BinOpExpr(Expr e1, Expr e2)
    {
        this.e1 = e1;
        this.e2 = e2;
    }
}
