package mn.jhn.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.*;

public class Auditor
{
    private static final Set<User> users;
    private static final Set<User> loggedInUsers;
    private static final Set<PrintWriter> writers;
    private static final Map<String, Map<InetAddress, Date>> serverBlocks;
    private static final Map<String, Date> loggedOutUsers;
    private static final Map<String, List<String>> userBlocks;

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
        loggedInUsers  = Collections.synchronizedSet(new HashSet<User>());
        loggedOutUsers = Collections.synchronizedMap(new HashMap<String, Date>());
        writers        = Collections.synchronizedSet(new HashSet<PrintWriter>());
        userBlocks     = Collections.synchronizedMap(new HashMap<String, List<String>>());
        serverBlocks   = Collections.synchronizedMap(new HashMap<String, Map<InetAddress, Date>>());
    }

    public synchronized static Set<User> getUsers()
    {
        return Collections.unmodifiableSet(users);
    }

    public static Set<User> getLoggedInUsers()
    {
        return loggedInUsers;
    }

    public static Map<String, Date> getLoggedOutUsers()
    {
        return loggedOutUsers;
    }

    public static Set<PrintWriter> getWriters()
    {
        return writers;
    }

    public static Map<String, Map<InetAddress, Date>> getServerBlocks()
    {
        return serverBlocks;
    }

    public synchronized static void registerClient(User user, PrintWriter out)
    {
        loggedInUsers.add(user);
        writers.add(out);
    }

    public synchronized static void unregisterClient(User user, PrintWriter out)
    {
        if (user != null)
        {
            loggedInUsers.remove(user);
            loggedOutUsers.put(user.getUsername(), new Date());
        }
        writers.remove(out);
    }

    public synchronized static void blockFromTo(String blocker, String blockee)
    {
        if (userBlocks.containsKey(blocker))
        {
            userBlocks.get(blocker).add(blockee);
        }
        else
        {
            ArrayList<String> blockedUsers = new ArrayList<String>();
            blockedUsers.add(blockee);
            userBlocks.put(blocker, blockedUsers);
        }
    }

    public synchronized static void unblockFromTo(String blocker, String blockee)
    {
        if (userBlocks.containsKey(blocker))
        {
            List<String> blockees = userBlocks.get(blocker);
            if (blockees.contains(blockee))
            {
                blockees.remove(blockee);
            }
        }
    }
}
