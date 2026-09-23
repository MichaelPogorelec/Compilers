/* COSC 4400 - Project 3 */
package Absyn;

/**
 * If Statements.
 */
public class IfStmt extends Stmt
{
    public Expr test;
    public Stmt thenStmt, elseStmt;

    public IfStmt(Expr test, Stmt thenStmt, Stmt elseStmt)
    {
        this.test     = test;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    public void accept(Visitor v) { v.visit(this); }
}
