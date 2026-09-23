/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Object Allocation.
 */
public class NewObjectExpr extends Expr
{
    public Type type;

    public NewObjectExpr(Type type) { this.type = type; }

    public void accept(Visitor v) { v.visit(this); }
}
