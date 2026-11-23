/**
 * COSC 4400 - Project 8
 * Reads the textual Project 7 output (DataFrag and ProcFrag blocks, as
 * printed by Translate.Main) back into fragments, so that the instruction
 * selector can be run on a saved translator output.
 * @authors Michael Pogorelec
 * Instructor Dr. Brylow
 * TA-BOT:MAILTO michael.pogorelec@marquette.edu
 */
package Parse;

import java.util.HashMap;
import java.util.LinkedList;

import Temp.Label;
import Temp.Temp;
import Tree.*;

public class ReadFrags
{
    private final String text;
    private int pos = 0;
    private final HashMap<String, Label> labels = new HashMap<String, Label>();
    private final Riscv.RiscvFrame factory;

    public ReadFrags(String text)
    {
	this.text = text;
	// Loading the frame class first creates the register temps, so the
	// temp numbers in the text line up with them.
	this.factory = new Riscv.RiscvFrame();
	restartTemps();
    }

    /* ---------------------------------------------------------------- */
    /* Scanning                                                         */
    /* ---------------------------------------------------------------- */

    private ParseException error(String msg)
    {
	int line = 1;
	for (int i = 0; i < pos && i < text.length(); i++)
	    if (text.charAt(i) == '\n') line++;
	return new ParseException(msg + " at line " + line);
    }

    private void skipSpace()
    {
	while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
	    pos++;
    }

    private static boolean wordChar(char c)
    {
	return Character.isLetterOrDigit(c) || c == '_' || c == '$'
	    || c == '-' || c == '.';
    }

    /** The next token, without consuming it; null at end of input. */
    private String peek()
    {
	int save = pos;
	String t = next();
	pos = save;
	return t;
    }

    private String next()
    {
	skipSpace();
	if (pos >= text.length()) return null;
	char c = text.charAt(pos);
	if (wordChar(c))
	{
	    int start = pos;
	    while (pos < text.length() && wordChar(text.charAt(pos))) pos++;
	    return text.substring(start, pos);
	}
	pos++;
	return String.valueOf(c);
    }

    private void expect(String tok) throws ParseException
    {
	String t = next();
	if (!tok.equals(t))
	    throw error("expected \"" + tok + "\" but found \"" + t + "\"");
    }

    private String word() throws ParseException
    {
	String t = next();
	if (t == null || !wordChar(t.charAt(0)))
	    throw error("expected a name but found \"" + t + "\"");
	return t;
    }

    /**
     * Labels named Ln are made fresh, in order of first appearance in the
     * text, so they are renumbered L0, L1, ... whatever their numbers were
     * in the translator's output.  Every other label (methods, vtables,
     * run-time routines) keeps its name.
     */
    private Label label() throws ParseException
    {
	String name = word();
	Label l = labels.get(name);
	if (l == null)
	{
	    if (name.equals("_BADPTR"))      l = factory.badPtr();
	    else if (name.equals("_BADSUB")) l = factory.badSub();
	    else if (name.matches("L[0-9]+")) l = new Label();
	    else                             l = new Label(name);
	    labels.put(name, l);
	}
	return l;
    }

    /** Number of the hardwired register temps: t0 .. t(registerLimit-1). */
    private int registerLimit;
    /** Fresh temps for the text's non-register temps, by their text name. */
    private java.util.HashMap<Integer, Temp> temps =
	new java.util.HashMap<Integer, Temp>();

    /**
     * Register temps are the machine's own.  Every other temp in the text
     * is made fresh, in order of first appearance, so they are numbered by
     * where they occur in the text, whatever their numbers were in the
     * translator's output.  Numbering restarts where the text's own first
     * non-register temp began.
     */
    private void restartTemps()
    {
	registerLimit = 0;
	for (Temp r : Riscv.RiscvFrame.registers)
	    registerLimit = Math.max(registerLimit, r.number() + 1);
	int base = Integer.MAX_VALUE;
	java.util.regex.Matcher m =
	    java.util.regex.Pattern.compile("\\bt([0-9]+)\\b").matcher(text);
	while (m.find())
	{
	    int n = Integer.parseInt(m.group(1));
	    if (n >= registerLimit && n < base) base = n;
	}
	if (base == Integer.MAX_VALUE) base = registerLimit;
	Temp.truncate(registerLimit);
	while (Temp.count() < base)
	    new Temp();
    }

    private Temp temp() throws ParseException
    {
	String name = word();
	if (!name.matches("t[0-9]+"))
	    throw error("expected a temp but found \"" + name + "\"");
	int n = Integer.parseInt(name.substring(1));
	if (n < registerLimit)
	    return Temp.forNumber(n);
	Temp t = temps.get(n);
	if (t == null)
	{
	    t = new Temp();
	    temps.put(n, t);
	}
	return t;
    }

    /* ---------------------------------------------------------------- */
    /* Fragments                                                        */
    /* ---------------------------------------------------------------- */

    public LinkedList<Translate.Frag> Program() throws ParseException
    {
	LinkedList<Translate.Frag> frags = new LinkedList<Translate.Frag>();
	for (;;)
	{
	    skipSpace();
	    if (pos >= text.length()) return frags;
	    if (text.startsWith("DataFrag(", pos))
		frags.add(dataFrag());
	    else if (text.startsWith("ProcFrag(", pos))
		frags.add(procFrag());
	    else
		throw error("expected DataFrag or ProcFrag");
	}
    }

    /** DataFrag(NL data NL ")" : the data is kept exactly as printed. */
    private Translate.Frag dataFrag() throws ParseException
    {
	pos += "DataFrag(".length();
	if (pos < text.length() && text.charAt(pos) == '\r') pos++;
	if (pos < text.length() && text.charAt(pos) == '\n') pos++;
	int end = text.indexOf("\n)", pos);
	if (end < 0) throw error("unterminated DataFrag");
	String data = text.substring(pos, end);
	pos = end + 2;
	return new Translate.DataFrag(data);
    }

    private Translate.Frag procFrag() throws ParseException
    {
	pos += "ProcFrag(".length();
	Frame.Frame frame = frame();
	Tree.Stm body = null;
	if (!")".equals(peek()))
	    body = stm();
	expect(")");
	return new Translate.ProcFrag(body, frame);
    }

    /**
     * RiscvFrame( name: Formals(...) Actuals(...) BadPtr(x) BadSub(y) )
     */
    private Frame.Frame frame() throws ParseException
    {
	expect("RiscvFrame");
	expect("(");
	String name = word();
	expect(":");
	Frame.Frame frame = factory.newFrame(Symbol.Symbol.symbol(name));
	frame.formals.clear();
	frame.actuals.clear();
	expect("Formals");
	accesses(frame.formals);
	expect("Actuals");
	accesses(frame.actuals);
	// Optional BadPtr(...) and BadSub(...) entries.
	while ("BadPtr".equals(peek()) || "BadSub".equals(peek()))
	{
	    next(); expect("("); label(); expect(")");
	}
	expect(")");
	return frame;
    }

    private void accesses(java.util.List<Frame.Access> list)
	throws ParseException
    {
	expect("(");
	while (!")".equals(peek()))
	{
	    String kind = word();
	    expect("(");
	    if (kind.equals("InReg"))
		list.add(new Riscv.InReg(temp()));
	    else if (kind.equals("InFrame"))
		list.add(new Riscv.InFrame(Integer.parseInt(word())));
	    else
		throw error("bad access \"" + kind + "\"");
	    expect(")");
	}
	expect(")");
    }

    /* ---------------------------------------------------------------- */
    /* Trees                                                            */
    /* ---------------------------------------------------------------- */

    private static int relop(String s) throws ParseException
    {
	String[] names = { "EQ", "NE", "LT", "GT", "LE", "GE",
			   "ULT", "ULE", "UGT", "UGE" };
	for (int i = 0; i < names.length; i++)
	    if (names[i].equals(s)) return i;
	throw new ParseException("bad CJUMP operator " + s);
    }

    private static int binop(String s) throws ParseException
    {
	String[] names = { "PLUS", "MINUS", "MUL", "DIV", "AND", "OR",
			   "LSHIFT", "RSHIFT", "ARSHIFT", "BITAND", "BITOR",
			   "BITXOR" };
	for (int i = 0; i < names.length; i++)
	    if (names[i].equals(s)) return i;
	throw new ParseException("bad BINOP operator " + s);
    }

    private Tree.Stm stm() throws ParseException
    {
	String k = word();
	expect("(");
	Tree.Stm result;
	if (k.equals("SEQ"))
	{
	    Tree.Stm l = stm();
	    Tree.Stm r = stm();
	    result = new SEQ(l, r);
	}
	else if (k.equals("LABEL"))
	    result = new Tree.LABEL(label());
	else if (k.equals("JUMP"))
	{
	    Tree.Exp e = exp();
	    if (e instanceof NAME)
		result = new JUMP(((NAME) e).label);
	    else
		result = new JUMP(e, new LinkedList<Label>());
	}
	else if (k.equals("CJUMP"))
	{
	    int op = relop(word());
	    Tree.Exp l = exp();
	    Tree.Exp r = exp();
	    Label t = label();
	    Label f = label();
	    result = new CJUMP(op, l, r, t, f);
	}
	else if (k.equals("MOVE"))
	{
	    Tree.Exp d = exp();
	    Tree.Exp s = exp();
	    result = new Tree.MOVE(d, s);
	}
	else if (k.equals("EXP"))
	    result = new Tree.EXP(exp());
	else
	    throw error("unknown statement " + k);
	expect(")");
	return result;
    }

    private Tree.Exp exp() throws ParseException
    {
	String k = word();
	expect("(");
	Tree.Exp result;
	if (k.equals("BINOP"))
	{
	    int op = binop(word());
	    Tree.Exp l = exp();
	    Tree.Exp r = exp();
	    result = new BINOP(op, l, r);
	}
	else if (k.equals("MEM"))
	    result = new MEM(exp());
	else if (k.equals("TEMP"))
	    result = new TEMP(temp());
	else if (k.equals("ESEQ"))
	{
	    Tree.Stm s = stm();
	    Tree.Exp e = exp();
	    result = new ESEQ(s, e);
	}
	else if (k.equals("NAME"))
	    result = new NAME(label());
	else if (k.equals("CONST"))
	{
	    String v = word();
	    try { result = new CONST(Integer.parseInt(v)); }
	    catch (NumberFormatException e)
	    { throw error("bad number " + v); }
	}
	else if (k.equals("CALL"))
	{
	    Tree.Exp f = exp();
	    LinkedList<Tree.Exp> args = new LinkedList<Tree.Exp>();
	    while (!")".equals(peek()))
		args.add(exp());
	    result = new CALL(f, args);
	}
	else
	    throw error("unknown expression " + k);
	expect(")");
	return result;
    }
}
