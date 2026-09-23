/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Integer types.
 */
public class IntegerType extends Type
{
    public IntegerType() { }

    public void accept(Visitor v) { v.visit(this); }
}
