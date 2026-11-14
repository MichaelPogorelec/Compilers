/**
 * COSC 4400 - Project 7
 * Translator driver: parses and type checks a MiniJava program read from
 * standard input, translates it to Tree intermediate code, and prints
 * each fragment.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */

package Translate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;

import Absyn.Program;
import Parse.MiniJavaParser;
import Parse.ParseException;
import Semant.TypeChecker;

public class Main 
{
    public static void main(String args[])
    {
	BufferedReader reader =
	    new BufferedReader(new InputStreamReader(System.in));

	// This builds on the earlier phases: the parser of Project 3 and
	// the type checker of Project 5, which annotates the AST that the
	// translator reads (class descriptors, method and field indexes).
	Program program = null;
	try
	{
	    program = new MiniJavaParser(reader).Goal();
	}
	catch (ParseException p)
	{
	    System.err.println(p.toString());
	    System.exit(-1);
	}

	// Type errors are reported by the checker; there is nothing to
	// translate if there are any.
	if (!new TypeChecker().check(program))
	    return;

	Translate translate = new Translate(new Riscv.RiscvFrame());
	program.accept(translate);
		
	PrintWriter writer = new PrintWriter(System.out);
		
	for (Iterator frags = translate.results().iterator(); 
	     frags.hasNext(); ) 
	{
	    Frag f = (Frag)frags.next();
	    if (f instanceof DataFrag) 
	    {
		writer.println("DataFrag(");
		writer.println(f);
		writer.println(")");
	    } 
	    else 
	    {
		writer.println("ProcFrag(");
		ProcFrag p = (ProcFrag)f;
		p.frame.printFrame(writer);
		if (p.body != null) 
		{
		    new Tree.Print(writer, p.body);
		    writer.println();
		}
		writer.println(")");
	    }
	}  
	writer.flush();
    }
}	
