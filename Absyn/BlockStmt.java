package Absyn;
import java.util.LinkedList;

/**
 * Curly brace delimited block of statements.
 */
public class BlockStmt extends Stmt
{
    public LinkedList<Stmt> stmts;

    public BlockStmt(LinkedList<Stmt> stmts) { this.stmts = stmts; }

    public void accept(Visitor v) { v.visit(this); }
    /** Visitor pattern dispatch for the translator. */
    public Translate.Exp accept(TreeVisitor v) { return v.visit(this); }
}
