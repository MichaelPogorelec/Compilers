/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Boolean types.
 */
public class BooleanType extends Type
{
    public BooleanType() { }

    public void accept(Visitor v) { v.visit(this); }
}
