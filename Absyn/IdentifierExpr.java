/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Reference to an identifier.
 */
public class IdentifierExpr extends AssignableExpr
{
    public String id;

    public IdentifierExpr(String id) { this.id = id; }

    public void accept(Visitor v) { v.visit(this); }
}
