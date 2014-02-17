package mn.jhn;

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

    @Override
    public String toString()
    {
        return command;
    }
}
