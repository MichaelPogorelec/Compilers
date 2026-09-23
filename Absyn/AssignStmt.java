/* COSC 4400 - Project 3 */
package Absyn;

/**
 * Assignment Statements.
 */
public class AssignStmt extends Stmt
{
    public AssignableExpr lhs;
    public Expr           rhs;

    public AssignStmt(AssignableExpr lhs, Expr rhs)
    {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(Visitor v) { v.visit(this); }
}
