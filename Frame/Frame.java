/**
 * COSC 4400 - Project 7
 * Modified for the AST-to-Tree translator and the instruction selector.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Frame;
import java.util.List;
import java.util.LinkedList;

/**
 * Holds information about local variables and formal parameters 
 * that have been allocated in this frame.
 */

public abstract class Frame
{
  /* To store the name of the function as a label*/
  public Temp.Label name;
  /* Formals - This will contain a list of Access*/	
  public List<Access> formals;	
  /* Actuals - where the caller places each argument (registers/stack) */
  public List<Access> actuals;

  public abstract Frame newFrame(Symbol.Symbol name);
  public abstract Access allocFormal();
  public abstract Access allocLocal();
  public abstract Temp.Temp FP();
  public abstract int wordSize();
  /* Function Symbol & Args list */
  public abstract Temp.Temp RV();

	
  /* Call to a routine in the runtime library, e.g. _allocRecord */
  public abstract Tree.Exp externalCall(Symbol.Symbol s, List<Tree.Exp> args);

  public abstract String string(Temp.Label label, String value);
  public abstract String record(Temp.Label label, int words);
  public abstract String record(Temp.Label label, List<Temp.Label> values);
  public abstract Temp.Label badPtr();
  public abstract Temp.Label badSub();
  public abstract String tempMap(Temp.Temp temp);
  public abstract void printFrame(java.io.PrintWriter pw);

  /** Adds the function prologue / epilogue statements to the traced body. */
  public abstract void procEntryExit1(List<Tree.Stm> body);
  /** Instruction selection for a traced, canonical statement list. */
  public abstract List<Assem.Instr> codeGen(List<Tree.Stm> stms);
  /* procEntryExit2/3 operate on Assem.Instr lists; the Assem package is not
   * part of this project, so they are omitted until it is introduced. */
}
