/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Boolean FALSE.
 */
public class FalseExpr extends Expr
{
    public FalseExpr() { }

    public void accept(Visitor v) { v.visit(this); }
}
