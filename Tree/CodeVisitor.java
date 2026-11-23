/**
 * COSC 4400 - Project 8
 * Visitor used by the instruction selector: expression nodes return the
 * temporary that holds their value, statement nodes emit code only.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
package Tree;
import Temp.Temp;

public abstract class CodeVisitor
{
    // statements
    public abstract void visit(SEQ n);
    public abstract void visit(LABEL n);
    public abstract void visit(JUMP n);
    public abstract void visit(CJUMP n);
    public abstract void visit(MOVE n);
    public abstract void visit(EXP n);

    // expressions (interlocking patterns): each returns its result temp
    public abstract Temp visit(BINOP n);
    public abstract Temp visit(MEM n);
    public abstract Temp visit(TEMP n);
    public abstract Temp visit(ESEQ n);
    public abstract Temp visit(NAME n);
    public abstract Temp visit(CONST n);
    public abstract Temp visit(CALL n);
}
