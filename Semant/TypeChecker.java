package Semant;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.io.PrintWriter;

import Absyn.AddExpr;
import Absyn.AndExpr;
import Absyn.ArrayExpr;
import Absyn.ArrayType;
import Absyn.AssignStmt;
import Absyn.AssignableExpr;
import Absyn.BlockStmt;
import Absyn.BooleanType;
import Absyn.CallExpr;
import Absyn.ClassDecl;
import Absyn.DivExpr;
import Absyn.EqualExpr;
import Absyn.Expr;
import Absyn.FalseExpr;
import Absyn.FieldExpr;
import Absyn.Formal;
import Absyn.GreaterExpr;
import Absyn.IdentifierExpr;
import Absyn.IdentifierType;
import Absyn.IfStmt;
import Absyn.IntegerLiteral;
import Absyn.IntegerType;
import Absyn.LesserExpr;
import Absyn.MethodDecl;
import Absyn.MulExpr;
import Absyn.NegExpr;
import Absyn.NewArrayExpr;
import Absyn.NewObjectExpr;
import Absyn.NotEqExpr;
import Absyn.NotExpr;
import Absyn.NullExpr;
import Absyn.OrExpr;
import Absyn.Program;
import Absyn.Stmt;
import Absyn.StringLiteral;
import Absyn.SubExpr;
import Absyn.ThisExpr;
import Absyn.TrueExpr;
import Absyn.Type;
import Absyn.VarDecl;
import Absyn.VoidDecl;
import Absyn.WhileStmt;
import Absyn.XinuCallExpr;
import Absyn.XinuCallStmt;
import Symbol.Table;
import Types.ARRAY;
import Types.BOOLEAN;
import Types.CLASS;
import Types.FIELD;
import Types.FUNCTION;
import Types.INT;
import Types.NIL;
import Types.OBJECT;
import Types.RECORD;
import Types.VOID;

/** Semantic analysis for the MiniJava AST. */
public class TypeChecker
{
    private final Table classes = new Table();
    private final Table variables = new Table();
    private final Map<Type, Types.Type> resolvedTypes =
        new IdentityHashMap<Type, Types.Type>();
    private final Set<Type> resolvedTypeNodes =
        java.util.Collections.newSetFromMap(new IdentityHashMap<Type, Boolean>());
    private final Map<String, ClassDecl> firstDeclarations =
        new HashMap<String, ClassDecl>();
    private final Set<CLASS> reportedCycles = new HashSet<CLASS>();
    private CLASS currentClass;
    private int errorCount;

    public TypeChecker()
    {
        classes.put("String", new CLASS("String"));
        classes.put("Thread", new CLASS("Thread"));
    }

    public boolean check(Program program)
    {
        collectClasses(program.classes);
        resolveParents(program.classes);
        checkInheritanceCycles(program.classes);
        collectMembers(program.classes);
        populateInstances(program.classes);
        checkOverrides(program.classes);
        for (ClassDecl declaration : program.classes)
        {
            if (firstDeclarations.get(declaration.name) == declaration)
                checkClass(declaration);
        }
            return errorCount == 0;
    }

        public void printDescriptors(Program program)
        {
            PrintWriter out = new PrintWriter(System.out);
            for (ClassDecl declaration : program.classes)
            {
                if (firstDeclarations.get(declaration.name) != declaration)
                    continue;
                printClass((CLASS) classes.get(declaration.name), out);
                out.println();
                out.println();
            }
            out.flush();
        }

        private void printClass(CLASS descriptor, PrintWriter out)
        {
            out.print("CLASS(" + descriptor.name);
            out.println();
            indent(out, 1);
            out.print(descriptor.parent == null ? "null" : descriptor.parent.name);
            out.println();
            printRecord(descriptor.methods, 1, out);
            out.println();
            printRecord(descriptor.fields, 1, out);
            out.println();
            printObject(descriptor.instance, out);
            out.print(")");
        }

        private void printObject(OBJECT object, PrintWriter out)
        {
            indent(out, 1);
            out.print("OBJECT(" + object.myClass.name);
            out.println();
            printRecord(object.methods, 2, out);
            out.println();
            printRecord(object.fields, 2, out);
            out.print(")");
        }

        private void printRecord(RECORD record, int depth, PrintWriter out)
        {
            indent(out, depth);
            out.print("RECORD(");
            if (record.size() == 0)
            {
                out.print(")");
                return;
            }
            out.println();
            int index = 0;
            for (FIELD field : record)
            {
                printField(field, depth + 1, out);
                if (++index < record.size())
                    out.println();
            }
            out.print(")");
        }

        private void printField(FIELD field, int depth, PrintWriter out)
        {
            indent(out, depth);
            out.print("FIELD(" + field.index + " " + field.name);
            out.println();
            printType(field.type, depth + 1, out);
            out.print(")");
        }

        private void printType(Types.Type type, int depth, PrintWriter out)
        {
            if (type instanceof FUNCTION)
            {
                FUNCTION function = (FUNCTION) type;
                indent(out, depth);
                out.print("FUNCTION(" + function.name);
                out.println();
                indent(out, depth + 1);
                printSimpleType(function.self, out);
                out.println();
                printRecord(function.formals, depth + 1, out);
                out.println();
                printType(function.result, depth + 1, out);
                out.print(")");
            }
            else if (type instanceof ARRAY)
            {
                indent(out, depth);
                out.print("ARRAY(");
                out.println();
                printType(((ARRAY) type).element, depth + 1, out);
                out.print(")");
            }
            else
            {
                indent(out, depth);
                printSimpleType(type, out);
            }
        }

        private void printSimpleType(Types.Type type, PrintWriter out)
        {
            if (type instanceof OBJECT)
                out.print("OBJECT(" + ((OBJECT) type).myClass.name + ")");
            else if (type instanceof INT)
                out.print("INT");
            else if (type instanceof BOOLEAN)
                out.print("BOOLEAN");
            else if (type instanceof VOID)
                out.print("VOID");
            else if (type instanceof NIL)
                out.print("NIL");
            else
                out.print(type == null ? "null" : type.toString());
        }

        private void indent(PrintWriter out, int depth)
        {
            for (int index = 0; index < depth; index++)
                out.print(' ');
        }

        private void populateInstances(AbstractList<ClassDecl> declarations)
        {
            Set<CLASS> populated = java.util.Collections.newSetFromMap(
                new IdentityHashMap<CLASS, Boolean>());
            for (ClassDecl declaration : declarations)
            {
                if (firstDeclarations.get(declaration.name) == declaration)
                    populateInstance((CLASS) classes.get(declaration.name), populated);
            }
        }

        private void populateInstance(CLASS descriptor, Set<CLASS> populated)
        {
            if (!populated.add(descriptor))
                return;
            if (descriptor.parent != null)
                populateInstance(descriptor.parent, populated);

            LinkedHashMap<String, Types.Type> methods =
                new LinkedHashMap<String, Types.Type>();
            if (descriptor.parent != null)
                for (FIELD method : descriptor.parent.instance.methods)
                    methods.put(method.name, method.type);
            for (FIELD method : descriptor.methods)
                methods.put(method.name, method.type);

            RECORD effectiveMethods = new RECORD();
            for (Map.Entry<String, Types.Type> method : methods.entrySet())
                effectiveMethods.put(method.getValue(), method.getKey());

            RECORD effectiveFields = new RECORD();
            if (descriptor.parent != null)
                for (FIELD field : descriptor.parent.instance.fields)
                    effectiveFields.put(field.type, field.name);
            for (FIELD field : descriptor.fields)
                effectiveFields.put(field.type, field.name);

            descriptor.instance.methods = effectiveMethods;
            descriptor.instance.fields = effectiveFields;
        }

    private void collectClasses(AbstractList<ClassDecl> declarations)
    {
        for (ClassDecl declaration : declarations)
        {
            if (firstDeclarations.containsKey(declaration.name))
            {
                error("duplicate class");
                continue;
            }
            firstDeclarations.put(declaration.name, declaration);
            classes.put(declaration.name, new CLASS(declaration.name));
        }
    }

    private void resolveParents(AbstractList<ClassDecl> declarations)
    {
        for (ClassDecl declaration : declarations)
        {
            if (firstDeclarations.get(declaration.name) != declaration ||
                declaration.parent == null)
                continue;
            CLASS child = (CLASS) classes.get(declaration.name);
            Object parent = classes.get(declaration.parent);
            if (!(parent instanceof CLASS))
                error("cannot resolve parent class " + declaration.parent);
            else
                child.parent = (CLASS) parent;
        }
    }

    private void checkInheritanceCycles(AbstractList<ClassDecl> declarations)
    {
        Map<CLASS, Integer> states = new IdentityHashMap<CLASS, Integer>();
        for (ClassDecl declaration : declarations)
        {
            if (firstDeclarations.get(declaration.name) == declaration)
                visitParent((CLASS) classes.get(declaration.name), states);
        }
    }

    private void visitParent(CLASS type, Map<CLASS, Integer> states)
    {
        Integer state = states.get(type);
        if (state != null)
            return;
        states.put(type, 1);
        if (type.parent != null)
        {
            Integer parentState = states.get(type.parent);
            if (parentState != null && parentState == 1)
            {
                if (reportedCycles.add(type.parent))
                    error("cyclic inheritance involving " + type.parent.name);
                type.parent = null;
            }
            else if (parentState == null)
                visitParent(type.parent, states);
        }
        states.put(type, 2);
    }

    private void collectMembers(AbstractList<ClassDecl> declarations)
    {
        for (ClassDecl declaration : declarations)
        {
            if (firstDeclarations.get(declaration.name) != declaration)
                continue;
            CLASS descriptor = (CLASS) classes.get(declaration.name);
            Set<String> names = new HashSet<String>();
            for (VarDecl field : declaration.fields)
            {
                if (!names.add(field.name))
                {
                    error(field.name + " is already defined in " + declaration.name);
                    continue;
                }
                descriptor.fields.put(resolveType(field.type), field.name);
            }
            for (MethodDecl method : declaration.methods)
            {
                if (!names.add(method.name))
                {
                    error(method.name + " is already defined in " + declaration.name);
                    continue;
                }
                RECORD formals = new RECORD();
                for (Formal formal : method.params)
                    formals.put(resolveType(formal.type), formal.name);
                Types.Type result = method.returnType == null
                    ? new VOID() : resolveType(method.returnType);
                descriptor.methods.put(new FUNCTION(method.name,
                    descriptor.instance, formals, result), method.name);
            }
        }
    }

    private void checkOverrides(AbstractList<ClassDecl> declarations)
    {
        for (ClassDecl declaration : declarations)
        {
            if (firstDeclarations.get(declaration.name) != declaration)
                continue;
            CLASS descriptor = (CLASS) classes.get(declaration.name);
            for (MethodDecl method : declaration.methods)
            {
                FUNCTION own = findDirectMethod(descriptor, method.name);
                if (own == null)
                    continue;
                FUNCTION inherited = findMethod(descriptor.parent, method.name);
                if (inherited != null && !sameSignature(own, inherited))
                    error("incompatible method override: " + method.name +
                          " in class " + declaration.name);
            }
        }
    }

    private boolean sameSignature(FUNCTION left, FUNCTION right)
    {
        if (!sameType(left.result, right.result) ||
            left.formals.size() != right.formals.size())
            return false;
        java.util.Iterator<FIELD> first = left.formals.iterator();
        java.util.Iterator<FIELD> second = right.formals.iterator();
        while (first.hasNext())
            if (!sameType(first.next().type, second.next().type))
                return false;
        return true;
    }

    private void checkClass(ClassDecl declaration)
    {
        currentClass = (CLASS) classes.get(declaration.name);
        for (MethodDecl method : declaration.methods)
            checkMethod(method);
        currentClass = null;
    }

    private void checkMethod(MethodDecl method)
    {
        variables.beginScope();
        Set<String> names = new HashSet<String>();
        for (Formal formal : method.params)
        {
            Types.Type type = resolveType(formal.type);
            if (!names.add(formal.name))
                error(formal.name + " is already defined in " + method.name);
            else
                variables.put(formal.name, type);
        }
        for (VarDecl local : method.locals)
        {
            Types.Type type = resolveType(local.type);
            if (!names.add(local.name))
                error(local.name + " is already defined in " + method.name);
            else
                variables.put(local.name, type);
            if (local.init != null)
                require(type, checkExpr(local.init));
        }
        for (Stmt statement : method.stmts)
            checkStmt(statement);
        if (!(method instanceof VoidDecl) && method.returnType != null)
            require(resolveType(method.returnType), checkExpr(method.returnVal));
        variables.endScope();
    }

    private Types.Type resolveType(Type type)
    {
        if (type == null)
            return null;
        if (resolvedTypeNodes.contains(type))
            return resolvedTypes.get(type);
        Types.Type result;
        if (type instanceof IntegerType)
            result = new INT();
        else if (type instanceof BooleanType)
            result = new BOOLEAN();
        else if (type instanceof ArrayType)
            result = new ARRAY(resolveType(((ArrayType) type).base));
        else if (type instanceof IdentifierType)
        {
            String name = ((IdentifierType) type).id;
            Object descriptor = classes.get(name);
            if (descriptor instanceof CLASS)
                result = ((CLASS) descriptor).instance;
            else
            {
                error("cannot resolve class " + name);
                result = null;
            }
        }
        else
            result = null;
        resolvedTypes.put(type, result);
        resolvedTypeNodes.add(type);
        return result;
    }

    private void checkStmt(Stmt statement)
    {
        if (statement instanceof BlockStmt)
        {
            for (Stmt child : ((BlockStmt) statement).stmts)
                checkStmt(child);
        }
        else if (statement instanceof AssignStmt)
        {
            AssignStmt assignment = (AssignStmt) statement;
            Types.Type target = checkAssignable(assignment.lhs);
            Types.Type value = checkExpr(assignment.rhs);
            require(target, value);
        }
        else if (statement instanceof IfStmt)
        {
            IfStmt conditional = (IfStmt) statement;
            require(new BOOLEAN(), checkExpr(conditional.test));
            checkStmt(conditional.thenStmt);
            if (conditional.elseStmt != null)
                checkStmt(conditional.elseStmt);
        }
        else if (statement instanceof WhileStmt)
        {
            WhileStmt loop = (WhileStmt) statement;
            require(new BOOLEAN(), checkExpr(loop.test));
            checkStmt(loop.body);
        }
        else if (statement instanceof XinuCallStmt)
        {
            for (Expr argument : ((XinuCallStmt) statement).args)
                checkExpr(argument);
        }
    }

    private Types.Type checkExpr(Expr expression)
    {
        if (expression == null)
            return null;
        if (expression instanceof IntegerLiteral)
            return new INT();
        if (expression instanceof TrueExpr || expression instanceof FalseExpr)
            return new BOOLEAN();
        if (expression instanceof StringLiteral)
            return classes.get("String") instanceof CLASS
                ? ((CLASS) classes.get("String")).instance : null;
        if (expression instanceof NullExpr)
            return new NIL();
        if (expression instanceof ThisExpr)
            return currentClass == null ? null : currentClass.instance;
        if (expression instanceof IdentifierExpr || expression instanceof FieldExpr ||
            expression instanceof ArrayExpr)
            return checkAssignable((AssignableExpr) expression);
        if (expression instanceof NewObjectExpr)
        {
            Types.Type type = resolveType(((NewObjectExpr) expression).type);
            return type;
        }
        if (expression instanceof NewArrayExpr)
        {
            NewArrayExpr allocation = (NewArrayExpr) expression;
            Types.Type element = resolveType(allocation.type);
            for (Expr dimension : allocation.dims)
                require(new INT(), checkExpr(dimension));
            return element == null ? null : new ARRAY(element);
        }
        if (expression instanceof CallExpr)
            return checkCall((CallExpr) expression);
        if (expression instanceof XinuCallExpr)
        {
            for (Expr argument : ((XinuCallExpr) expression).args)
                checkExpr(argument);
            return new INT();
        }
        if (expression instanceof NegExpr)
        {
            Types.Type operand = checkExpr(((NegExpr) expression).operand);
            if (operand != null && !(operand instanceof INT))
                operatorError("-", operand, null);
            return operand == null ? null : new INT();
        }
        if (expression instanceof NotExpr)
        {
            Types.Type operand = checkExpr(((NotExpr) expression).operand);
            if (operand != null && !(operand instanceof BOOLEAN))
                operatorError("!", operand, null);
            return operand == null ? null : new BOOLEAN();
        }
        if (expression instanceof AddExpr)
            return checkBinary((AddExpr) expression, "+", new INT(), new INT());
        if (expression instanceof SubExpr)
            return checkBinary((SubExpr) expression, "-", new INT(), new INT());
        if (expression instanceof MulExpr)
            return checkBinary((MulExpr) expression, "*", new INT(), new INT());
        if (expression instanceof DivExpr)
            return checkBinary((DivExpr) expression, "/", new INT(), new INT());
        if (expression instanceof AndExpr)
            return checkBinary((AndExpr) expression, "&&", new BOOLEAN(), new BOOLEAN());
        if (expression instanceof OrExpr)
            return checkBinary((OrExpr) expression, "||", new BOOLEAN(), new BOOLEAN());
        if (expression instanceof LesserExpr)
            return checkBinary((LesserExpr) expression, "<", new INT(), new BOOLEAN());
        if (expression instanceof GreaterExpr)
            return checkBinary((GreaterExpr) expression, ">", new INT(), new BOOLEAN());
        if (expression instanceof EqualExpr)
            return checkEquality((EqualExpr) expression, "==");
        if (expression instanceof NotEqExpr)
            return checkEquality((NotEqExpr) expression, "!=");
        return null;
    }

    private Types.Type checkAssignable(AssignableExpr expression)
    {
        if (expression instanceof IdentifierExpr)
        {
            String name = ((IdentifierExpr) expression).id;
            if (variables.contains(name))
                return (Types.Type) variables.get(name);
            FIELD field = findField(currentClass, name);
            if (field != null)
                return field.type;
            error("cannot resolve symbol " + name);
            return null;
        }
        if (expression instanceof FieldExpr)
        {
            FieldExpr access = (FieldExpr) expression;
            Types.Type receiver = checkExpr(access.object);
            CLASS descriptor = objectClass(receiver);
            if (receiver == null)
                return null;
            if (descriptor == null)
            {
                error("target not object, type " + typeName(receiver));
                return null;
            }
            FIELD field = findField(descriptor, access.name);
            if (field == null)
                error("cannot resolve symbol " + access.name);
            return field == null ? null : field.type;
        }
        if (expression instanceof ArrayExpr)
        {
            ArrayExpr access = (ArrayExpr) expression;
            Types.Type array = checkExpr(access.array);
            require(new INT(), checkExpr(access.index));
            if (array == null)
                return null;
            if (!(array instanceof ARRAY))
            {
                error("array required, but " + typeName(array) + " found");
                return null;
            }
            return ((ARRAY) array).element;
        }
        return null;
    }

    private Types.Type checkCall(CallExpr call)
    {
        Types.Type receiver = checkExpr(call.object);
        CLASS descriptor = objectClass(receiver);
        if (receiver != null && descriptor == null)
            error("target not object, type " + typeName(receiver));
        FUNCTION method = descriptor == null ? null : findMethod(descriptor, call.method);
        if (method == null && descriptor != null)
            error("cannot resolve method " + call.method);

        ArrayList<Types.Type> arguments = new ArrayList<Types.Type>();
        for (Expr argument : call.args)
            arguments.add(checkExpr(argument));
        if (method == null)
            return null;
        if (arguments.size() != method.formals.size())
        {
            error("mismatch in number of arguments");
            return method.result;
        }
        java.util.Iterator<FIELD> formals = method.formals.iterator();
        for (Types.Type argument : arguments)
            require(formals.next().type, argument);
        return method.result;
    }

    private Types.Type checkBinary(Absyn.BinOpExpr expression, String operator,
                                   Types.Type operandType, Types.Type resultType)
    {
        Types.Type left = checkExpr(expression.e1);
        Types.Type right = checkExpr(expression.e2);
        boolean leftValid = left == null || sameType(left, operandType);
        boolean rightValid = right == null || sameType(right, operandType);
        if (!leftValid || !rightValid)
        {
            if (left != null && right != null)
                operatorError(operator, left, right);
            else if (left != null)
                operatorError(operator, left, null);
            else if (right != null)
                operatorError(operator, right, null);
            return null;
        }
        return left == null || right == null ? null : resultType;
    }

    private Types.Type checkEquality(Absyn.BinOpExpr expression, String operator)
    {
        Types.Type left = checkExpr(expression.e1);
        Types.Type right = checkExpr(expression.e2);
        if (left == null || right == null)
            return null;
        if (!assignable(left, right) && !assignable(right, left))
        {
            operatorError(operator, left, right);
            return null;
        }
        return new BOOLEAN();
    }

    private void require(Types.Type expected, Types.Type actual)
    {
        if (expected != null && actual != null && !assignable(actual, expected))
            error("incompatible types: " + typeName(expected) +
                  " required, but " + typeName(actual) + " found");
    }

    private boolean assignable(Types.Type actual, Types.Type expected)
    {
        return actual != null && expected != null && actual.coerceTo(expected);
    }

    private boolean sameType(Types.Type left, Types.Type right)
    {
        return left != null && right != null &&
               left.coerceTo(right) && right.coerceTo(left);
    }

    private CLASS objectClass(Types.Type type)
    {
        if (type instanceof OBJECT)
            return ((OBJECT) type).myClass;
        if (type instanceof CLASS)
            return (CLASS) type;
        return null;
    }

    private FIELD findField(CLASS descriptor, String name)
    {
        Set<CLASS> seen = new HashSet<CLASS>();
        for (CLASS current = descriptor; current != null && seen.add(current);
             current = current.parent)
        {
            FIELD field = current.fields.get(name);
            if (field != null)
                return field;
        }
        return null;
    }

    private FUNCTION findDirectMethod(CLASS descriptor, String name)
    {
        FIELD field = descriptor.methods.get(name);
        return field != null && field.type instanceof FUNCTION
            ? (FUNCTION) field.type : null;
    }

    private FUNCTION findMethod(CLASS descriptor, String name)
    {
        Set<CLASS> seen = new HashSet<CLASS>();
        for (CLASS current = descriptor; current != null && seen.add(current);
             current = current.parent)
        {
            FUNCTION method = findDirectMethod(current, name);
            if (method != null)
                return method;
        }
        return null;
    }

    private void operatorError(String operator, Types.Type first, Types.Type second)
    {
        String message = "operator " + operator + " cannot be applied to " +
            typeName(first);
        if (second != null)
            message += ", " + typeName(second);
        error(message);
    }

    private String typeName(Types.Type type)
    {
        if (type instanceof OBJECT)
            return ((OBJECT) type).myClass.name;
        if (type instanceof CLASS)
            return ((CLASS) type).name;
        if (type instanceof NIL)
            return "null";
        if (type instanceof ARRAY)
            return typeName(((ARRAY) type).element) + "[]";
        return type == null ? "<unknown>" : type.toString();
    }

    private void error(String message)
    {
        errorCount++;
        System.out.println("ERROR " + message);
    }
}