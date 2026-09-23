/* COSC 4400 - Project 3 */
package Absyn;
import java.util.LinkedList;

/**
 * Array Allocation.
 */
public class NewArrayExpr extends Expr
{
    public Type             type;
    public LinkedList<Expr> dims;

    public NewArrayExpr(Type type, LinkedList<Expr> dims)
    {
        this.type = type;
        this.dims = dims;
    }

    public void accept(Visitor v) { v.visit(this); }
}
