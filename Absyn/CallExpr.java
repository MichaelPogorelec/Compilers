package Absyn;
import java.util.LinkedList;

/**
 * Method Call.
 */
public class CallExpr extends Expr
{
    public Expr             object;
    public String           method;
    public LinkedList<Expr> args;
    /** Index of the method in the receiver's methods record; set by the type checker. */
    public int index = -1;

    public CallExpr(Expr object, String method, LinkedList<Expr> args)
    {
        this.object = object;
        this.method = method;
        this.args   = args;
    }

    public void accept(Visitor v) { v.visit(this); }
}
