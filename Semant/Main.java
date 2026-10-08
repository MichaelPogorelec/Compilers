package Semant;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
            new TypeChecker().check(program);
        }
        catch (ParseException exception)
        {
            System.out.println(exception.toString());
            System.exit(-1);
        }
    }
}