package Absyn;

/**
 * Field lookup on object ref.
 */
public class FieldExpr extends AssignableExpr
{
    public Expr   object;
    public String name;

    public FieldExpr(Expr object, String name)
    {
        this.object = object;
        this.name   = name;
    }

    public void accept(Visitor v) { v.visit(this); }
}
