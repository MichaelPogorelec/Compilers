/**
 * COSC 4400 - Project 8
 * Instruction selection driver: obtains the Tree fragments (by running the
 * parser, type checker and translator of the earlier projects on a MiniJava
 * program, or by reading Project 7 IR text), canonicalizes and trace
 * schedules each procedure, and selects RISC-V instructions for it.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 2009, Dennis Brylow
 * All rights reserved.  */

package Assem;
import java.io.PrintWriter;
import java.util.Iterator;

import Parse.*;

import java.util.HashMap; 
import java.util.List;
import java.util.LinkedList;

public class Main 
{
    static final String VERSION = "COSC 4400" +
		" Instruction Selection" +
		" Fall 2026";

	public static void usage()
	{
		System.err.println("Usage:  [-options] [file.java | file.ir | -]");
		System.err.println("\t(with no file, the program is read from standard input)");
		System.err.println("\twhere options include:");
		System.err.println("\t-a\tOutput Assembly stage");
		System.err.println("\t-b\tOutput Basic Block stage");
		System.err.println("\t-c\tOutput Canonical Linearization stage");
		System.err.println("\t-p\tOutput Prologue / Epilogue stage");
		System.err.println("\t-t\tOutput Trace Scheduling stage");
		System.err.println("\t-v\tPrint version and exit");
		System.exit(-2);
	}


	/**
	 * The fragments to select instructions for.  Input that starts with
	 * DataFrag( or ProcFrag( is Project 7 IR text; anything else is a
	 * MiniJava program, which is run through the parser, the type checker
	 * and the translator.  Returns null when the program does not type
	 * check (the checker has already reported the errors).
	 */
	static List<Translate.Frag> readFrags(java.io.Reader reader)
		throws ParseException
	{
		java.io.BufferedReader in = new java.io.BufferedReader(reader);
		StringBuilder text = new StringBuilder();
		try
		{
			char[] buf = new char[8192];
			int n;
			while ((n = in.read(buf)) > 0) text.append(buf, 0, n);
		}
		catch (java.io.IOException e)
		{
			System.err.println(e.toString());
			System.exit(-1);
		}
		String input = text.toString();
		String head = input.trim();

		if (head.startsWith("DataFrag(") || head.startsWith("ProcFrag("))
		{
			Temp.Label.reset();
			return new Parse.ReadFrags(input).Program();
		}

		Absyn.Program program =
			new Parse.MiniJavaParser(new java.io.StringReader(input)).Goal();
		if (!new Semant.TypeChecker().check(program))
			return null;
		Translate.Translate translate =
			new Translate.Translate(new Riscv.RiscvFrame());
		program.accept(translate);

		// Hand the translator's output to the selector as text, exactly
		// as if it had been saved and read back: that renumbers labels
		// by order of appearance.
		java.io.StringWriter text2 = new java.io.StringWriter();
		java.io.PrintWriter out = new java.io.PrintWriter(text2);
		Riscv.RiscvFrame.headerName = "RiscvFrame";
		for (Translate.Frag f : translate.results())
		{
			if (f instanceof Translate.DataFrag)
			{
				out.println("DataFrag(");
				out.println(f);
				out.println(")");
			}
			else
			{
				Translate.ProcFrag p = (Translate.ProcFrag) f;
				out.println("ProcFrag(");
				p.frame.printFrame(out);
				if (p.body != null)
				{
					new Tree.Print(out, p.body);
					out.println();
				}
				out.println(")");
			}
		}
		out.flush();
		Temp.Label.reset();
		return new Parse.ReadFrags(text2.toString()).Program();
	}

    public static void main(String args[])
    {
		boolean assemOutput = false,
			canonOutput = false,
			blockOutput = false,
			traceOutput = false,
			prologOutput = false;
		java.io.Reader reader = null;
		
		for (int i = 0; i < args.length; i++)
	    {
			if (args[i].equals("-v"))
		    {
				System.out.println(VERSION);
				System.exit(0);
		    }
			else if (args[i].equals("-a"))
			{   assemOutput = true;   }
			else if (args[i].equals("-b"))
			{   blockOutput = true;   }
			else if (args[i].equals("-c"))
			{   canonOutput = true;   }
			else if (args[i].equals("-p"))
			{   prologOutput = true;   }
			else if (args[i].equals("-t"))
			{   traceOutput = true;   }
			else if (args[i].equals("-"))
		    {
				java.io.InputStreamReader isr = 
					new java.io.InputStreamReader(System.in);
				reader = new java.io.BufferedReader(isr);
		    }
			else
		    {
				try
			    {
					reader = new java.io.FileReader(args[i]);
			    }
				catch (java.io.FileNotFoundException fnfe)
			    {
					System.err.println("File Not Found: " + args[i]);
					System.exit(-1);
			    }
		    }
	    }
		
		// No file argument: read standard input.
		if (null == reader)
		    reader = new java.io.BufferedReader(
			new java.io.InputStreamReader(System.in));
		
		List<Translate.Frag> frags = null;

		try
	    {
			frags = readFrags(reader);
	    }
		catch (ParseException p)
	    {
			System.err.println(p.toString());
			System.exit(-1);
	    }
		if (frags == null) return;

		// The selector's output names the frame class in full.
		Riscv.RiscvFrame.headerName = "Riscv.RiscvFrame";
		PrintWriter writer = new PrintWriter(System.out);
		
		for (Translate.Frag frag : frags)
		{
			if (frag instanceof Translate.DataFrag) 
			{
				writer.println("DataFrag(");
				writer.println(frag);
				writer.println(")");
			} 
			else 
			{
				Translate.ProcFrag p = (Translate.ProcFrag)frag;
				List<Tree.Stm> traced = new LinkedList<Tree.Stm>();
				if (p.body != null) 
				{
					LinkedList<Tree.Stm> stms = Canon.Canon.linearize(p.body);
					if (canonOutput)
					{
						writer.println("/* **** CANONICAL TREES **** */");
						for (Tree.Stm s : stms)
						{
							new Tree.Print(writer, s);
							writer.println();
						}
						writer.flush();
					}
					Canon.BasicBlocks blocks = new Canon.BasicBlocks(stms);
					if (blockOutput)
					{
						writer.println("/* **** BASIC BLOCKS **** */");
						for (LinkedList<Tree.Stm> slist : blocks.blocks)
						{
							for (Tree.Stm s : slist)
							{
								new Tree.Print(writer, s);
								writer.println();
							}
						}
						new Tree.Print(writer, new Tree.LABEL(blocks.done));
						writer.flush();
					}
					new Canon.TraceSchedule(blocks, traced);
					if (traceOutput)
					{
						writer.println("/* **** TRACE SCHEDULED **** */");
						for (Tree.Stm s : traced)
						{
							new Tree.Print(writer, s);
							writer.println();
						}
						writer.flush();						
					}
				}
				p.frame.procEntryExit1(traced);
				if (prologOutput)
				{
					writer.println("/* **** PROLOGUE / EPILOGUE **** */");
					for (Tree.Stm s : traced)
					{
						new Tree.Print(writer, s);
						writer.println();
					}
					writer.flush();
				}

				List<Assem.Instr> code = p.frame.codeGen(traced);
				if (assemOutput)
				{
					for (Assem.Instr i : code)
					{
						writer.println(i);
					}
				}

				writer.println("ProcFrag(");
				p.frame.printFrame(writer);
				for (Assem.Instr i : code)
				{
					i.output(writer);
				}
				writer.println(")");
			}
		}  
		writer.flush();
	}
}	

