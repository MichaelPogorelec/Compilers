/**
 * COSC 4400 - Project 7
 * Interned names, used by the frame and translator to name methods and
 * external (runtime) routines.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
package Symbol;

import java.util.HashMap;

/** An interned name: two symbols with the same text are the same object. */
public class Symbol
{
    private final String name;

    private Symbol(String name) { this.name = name; }

    public String toString() { return name; }

    private static final HashMap<String, Symbol> table =
        new HashMap<String, Symbol>();

    /** Returns the unique Symbol for the given text. */
    public static Symbol symbol(String name)
    {
        Symbol s = table.get(name);
        if (s == null)
        {
            s = new Symbol(name);
            table.put(name, s);
        }
        return s;
    }
}
