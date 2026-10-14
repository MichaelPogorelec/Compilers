package Absyn;

/**
 * Field lookup on object ref.
 */
public class FieldExpr extends AssignableExpr
{
    public Expr   object;
    public String name;
    /** Index of the field in the receiver's fields record (-1 for array length); set by the type checker. */
    public int index = -1;

    public FieldExpr(Expr object, String name)
    {
        this.object = object;
        this.name   = name;
    }

    public void accept(Visitor v) { v.visit(this); }
}
