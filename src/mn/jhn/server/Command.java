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

    Command(String command)
    {
        this.command = command;
    }

    public static Set<String> getCommands()
    {
        final Set<String> commands = Collections.unmodifiableSet(new HashSet<String>());
        for (Command c : Command.values())
        {
            commands.add(c.toString());
        }
        return commands;
    }

    @Override
    public String toString()
    {
        return command;
    }
}
