/**
 * COSC 5400 - Project 5
 * Mini Java compiler that compiles through the type checking semantic analysis phase
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 * */

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
            TypeChecker checker = new TypeChecker();
            if (checker.check(program))
                checker.printDescriptors(program);
        }
        catch (ParseException exception)
        {
            System.out.println(exception.toString());
            System.exit(-1);
        }
    }
}
