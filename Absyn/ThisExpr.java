/* COSC 4400 - Project 3 */
package Absyn;

/**
 * The "this" reference.
 */
public class ThisExpr extends Expr
{
    public ThisExpr() { }

    public void accept(Visitor v) { v.visit(this); }
}
