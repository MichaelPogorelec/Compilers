/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Boolean TRUE.
 */
public class TrueExpr extends Expr
{
    public TrueExpr() { }

    public void accept(Visitor v) { v.visit(this); }
}
