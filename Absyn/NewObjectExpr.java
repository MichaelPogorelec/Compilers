package Absyn;

/**
 * Object Allocation.
 */
public class NewObjectExpr extends Expr
{
    public Type type;

    public NewObjectExpr(Type type) { this.type = type; }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
