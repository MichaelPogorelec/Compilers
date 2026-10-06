package Absyn;
import java.util.LinkedList;

/**
 * Void Method Declaration struct.
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
