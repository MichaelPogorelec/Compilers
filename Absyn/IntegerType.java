package Absyn;

/**
 * Integer types.
 */
public class IntegerType extends Type
{
    public IntegerType() { }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
