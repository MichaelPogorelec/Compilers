/**
 * COSC 4400 - Project 8
 * Temporaries and labels, with support for reading them back from IR text.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
/* Copyright (C) 1997-2003, Purdue Research Foundation of Purdue University.
 * All rights reserved.  */
package Temp;

/**
 * Class used to create a temp, an abstract name for a local variable.
 */

public class Temp 
{
    private static int count;
    private int num;
    /** Every temp ever made, by number, so that numbers read back from IR
     *  text map to the same objects as the register temps. */
    private static java.util.ArrayList<Temp> all = new java.util.ArrayList<Temp>();
    public String toString() {return "t" + num;}
    public Temp() 
	{ 
		num = count++; 
		all.add(this);
	}

    /** The temp numbered n, making temps up to n if there are not yet. */
    public static Temp forNumber(int n)
	{
		while (count <= n) new Temp();
		return all.get(n);
	}
    public int hashCode() { return num; }
	public boolean spillTemp = false;
    public int spillCost;

}
