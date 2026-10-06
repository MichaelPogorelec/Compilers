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

    public CallExpr(Expr object, String method, LinkedList<Expr> args)
    {
        this.object = object;
        this.method = method;
        this.args   = args;
    }

    public void accept(Visitor v) { v.visit(this); }
}
