/* COSC 4400 - Project 3 */
package Absyn;
import java.util.LinkedList;

/**
 * Thread Class Declaration Blocks
 */
public class ThreadDecl extends ClassDecl
{
    public ThreadDecl(String name, LinkedList<VarDecl> fields,
                      LinkedList<MethodDecl> methods)
    {
        super(name, "Thread", fields, methods);
    }

    public void accept(Visitor v) { v.visit(this); }
}
