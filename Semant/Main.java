package Semant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import Absyn.PrintVisitor;
import Absyn.Program;
import Parse.MiniJavaParser;
import Parse.ParseException;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            Program program = new MiniJavaParser(
                new BufferedReader(new InputStreamReader(System.in))).Goal();
            TypeChecker checker = new TypeChecker();
            if (checker.check(program))
            {
                PrintWriter writer = new PrintWriter(System.out);
                new PrintVisitor(writer).visit(program);
                writer.flush();
            }
        }
        catch (ParseException exception)
        {
            System.out.println(exception.toString());
            System.exit(-1);
        }
    }
}
