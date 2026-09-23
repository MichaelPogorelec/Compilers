/* COSC 4400 - Project 3 */
package Absyn;
import java.util.LinkedList;

/**
 * Run (Void) Method Declaration structure.
 */
public class VoidDecl extends MethodDecl
{
    public VoidDecl(String name, LinkedList<VarDecl> locals,
                    LinkedList<Stmt> stmts)
    {
        super(null, false, name, new LinkedList<Formal>(), locals, stmts, null);
    }

    public void accept(Visitor v) { v.visit(this); }
}
