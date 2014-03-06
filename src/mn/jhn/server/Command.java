package mn.jhn.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Command
{
    WHOELSE("whoelse", 0),
    WHOLASTHR("wholasthr", 0),
    BROADCAST("broadcast", 1),
    MESSAGE("message", 2),
    BLOCK("block", 1),
    UNBLOCK("unblock", 1),
    LOGOUT("logout", 0);

    private final String name;
    private final int arity;
    private final static Set<String> commands;

    static
    {
        commands = new HashSet<String>();
        for (Command c : Command.values())
        {
            commands.add(c.toString());
        }
        Collections.unmodifiableSet(commands);
    }

    Command(String name, int arity)
    {
        this.name = name;
        this.arity = arity;
    }

    public static Set<String> getCommands()
    {
        return commands;
    }

    public String getName()
    {
        return name;
    }

    public int getArity()
    {
        return arity;
    }

    public static Command getCommand(String name)
    {
        for (Command c : Command.values())
        {
            if (c.getName().equals(name))
            {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
