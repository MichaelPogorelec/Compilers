/* COSC 4400 - Project 3 */
package Absyn;

/**
 * The "null" expression.
 */
public class NullExpr extends Expr
{
    public NullExpr() { }

    public void accept(Visitor v) { v.visit(this); }
}
