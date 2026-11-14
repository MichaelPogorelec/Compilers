package Absyn;

/**
 * While Statements.
 */
public class WhileStmt extends Stmt
{
    public Expr test;
    public Stmt body;

    public WhileStmt(Expr test, Stmt body)
    {
        this.test = test;
        this.body = body;
    }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
