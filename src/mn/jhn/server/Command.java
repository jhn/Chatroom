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
    LOGOUT("logout", 0),
    WEATHER("weather", 1),
    MYIP("myip", 0);

    private final String name;
    private final int arity;
    private final static Set<String> COMMANDS;

    static
    {
        COMMANDS = new HashSet<String>();
        for (Command c : Command.values())
        {
            COMMANDS.add(c.toString());
        }
        Collections.unmodifiableSet(COMMANDS);
    }

    Command(String name, int arity)
    {
        this.name = name;
        this.arity = arity;
    }

    public static Set<String> getCommands()
    {
        return COMMANDS;
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

    public static boolean checkArityForCommand(Command c, int arity)
    {
        return c.getArity() == arity;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
