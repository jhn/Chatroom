package mn.jhn.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.*;

public class Auditor
{
    private static final Set<User> users;
    private static final Set<User> loggedInUsers =
            Collections.synchronizedSet(new HashSet<User>());
    private static final Set<PrintWriter> writers =
            Collections.synchronizedSet(new HashSet<PrintWriter>());
    private static final Map<String, Map<InetAddress, Date>> blockedUsers =
            Collections.synchronizedMap(new HashMap<String, Map<InetAddress, Date>>());

    static
    {
        try
        {
            users = Utils.loadUsersFromFile("resources/user_pass.txt");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Couldn't load users.");
        }
    }

    public synchronized static Set<User> getUsers()
    {
        return Collections.unmodifiableSet(users);
    }

    public static Set<User> getLoggedInUsers()
    {
        return loggedInUsers;
    }

    public static Set<PrintWriter> getWriters()
    {
        return writers;
    }

    public static Map<String, Map<InetAddress, Date>> getBlockedUsers()
    {
        return blockedUsers;
    }
}
