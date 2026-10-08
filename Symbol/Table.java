package Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** A stack of symbol scopes, searched from the innermost scope outward. */
public class Table
{
    private final ArrayList<Map<String, Object>> scopes =
        new ArrayList<Map<String, Object>>();

    public Table()
    {
        beginScope();
    }

    public Object put(String name, Object value)
    {
        return scopes.get(scopes.size() - 1).put(name, value);
    }

    public Object get(String name)
    {
        for (int index = scopes.size() - 1; index >= 0; index--)
        {
            Map<String, Object> scope = scopes.get(index);
            if (scope.containsKey(name))
                return scope.get(name);
        }
        return null;
    }

    public boolean contains(String name)
    {
        for (int index = scopes.size() - 1; index >= 0; index--)
            if (scopes.get(index).containsKey(name))
                return true;
        return false;
    }

    public boolean containsCurrent(String name)
    {
        return scopes.get(scopes.size() - 1).containsKey(name);
    }

    public void beginScope()
    {
        scopes.add(new HashMap<String, Object>());
    }

    public void endScope()
    {
        if (scopes.size() <= 1)
            throw new IllegalStateException("cannot remove the global scope");
        scopes.remove(scopes.size() - 1);
    }
}