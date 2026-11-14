package Absyn;

/**
 * Boolean types.
 */
public class BooleanType extends Type
{
    public BooleanType() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
