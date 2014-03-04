package mn.jhn.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum Command
{
    WHOELSE("whoelse"),
    WHOLASTHR("wholasthr"),
    BROADCAST("broadcast"),
    MESSAGE("message"),
    BLOCK("block"),
    UNBLOCK("unblock"),
    LOGOUT("logout");

    private final String command;
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

    Command(String command)
    {
        this.command = command;
    }

    public static Set<String> getCommands()
    {
        return commands;
    }

    @Override
    public String toString()
    {
        return command;
    }
}
