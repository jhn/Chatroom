package mn.jhn.server;

import java.util.*;

public enum Command
{
    WHOELSE("whoelse", 0, "See who else is connected right now."),
    WHOLASTHR("wholasthr", 0, "See who was here during the last hour."),
    BROADCAST("broadcast", 1, "Send a message to everyone."),
    MESSAGE("message", 2, "Send someone a private message."),
    BLOCK("block", 1, "Block that annoying someone."),
    UNBLOCK("unblock", 1, "Unblock a previously blocked user."),
    LOGOUT("logout", 0, "Log out of the chatroom."),
    WEATHER("weather", 1, "Get the weather for a city."),
    MYIP("myip", 0, "Find out what your IP address is."),
    AWAY("away", 1, "Set your status as away."),
    BACK("back", 0, "Set your status as online after being away."),
    STATUSES("statuses", 0, "Display statuses of all logged in users."),
    HELP("help", 0, "Display all available commands.");

    private final String name;
    private final int arity;
    private final String description;
    private final static Set<String> COMMANDS;

    // Initializes list of command names
    static
    {
        COMMANDS = new HashSet<String>();
        for (Command c : Command.values())
        {
            COMMANDS.add(c.toString());
        }
        Collections.unmodifiableSet(COMMANDS);
    }

    Command(String name, int arity, String description)
    {
        this.name = name;
        this.arity = arity;
        this.description = description;
    }

    public static Map<String, String> getCommandsWithDescriptions()
    {
        Map<String, String> commands = new HashMap<String, String>();
        for (Command command : Command.values())
        {
            commands.put(command.getName(), command.getDescription());
        }
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

    public String getDescription()
    {
        return description;
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
