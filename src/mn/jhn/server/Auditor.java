package mn.jhn.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.*;

public class Auditor
{
    private static final Set<User> users;
    private static final Map<String, PrintWriter> loggedInUsers;
    private static final Map<String, Map<InetAddress, Date>> serverBlocks;
    private static final Map<String, Date> loggedOutUsers;
    private static final Map<String, Set<String>> userBlocks;

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
        loggedInUsers   = Collections.synchronizedMap(new HashMap<String, PrintWriter>());
        loggedOutUsers  = Collections.synchronizedMap(new HashMap<String, Date>());
        userBlocks      = Collections.synchronizedMap(new HashMap<String, Set<String>>());
        serverBlocks    = Collections.synchronizedMap(new HashMap<String, Map<InetAddress, Date>>());
    }

    public synchronized static Set<User> getUsers()
    {
        return new HashSet<User>(users);
    }

    public synchronized static Map<String, PrintWriter> getLoggedInUsers()
    {
        return new HashMap<String, PrintWriter>(loggedInUsers);
    }

    public synchronized static Set<String> getLoggedInUsernames()
    {
        return new HashSet<String>(loggedInUsers.keySet());
    }

    public synchronized static Set<PrintWriter> getWriters()
    {
        return new HashSet<PrintWriter>(loggedInUsers.values());
    }

    public synchronized static Map<String, Date> getLoggedOutUsers()
    {
        return new HashMap<String, Date>(loggedOutUsers);
    }

    public static Map<String, Map<InetAddress, Date>> getServerBlocks()
    {
        return serverBlocks;
    }

    public synchronized static void registerClient(User user, PrintWriter out)
    {
        loggedInUsers.put(user.getUsername(), out);
    }

    public synchronized static void unregisterClient(User user)
    {
        if (user != null)
        {
            loggedInUsers.remove(user.getUsername());
            loggedOutUsers.put(user.getUsername(), new Date());
        }
    }

    public synchronized static void blockFromTo(String blocker, String blockee)
    {
        // If the blocker has already blocked some people
        if (userBlocks.containsKey(blocker))
        {
            // Just add the new blockee to the set
            userBlocks.get(blocker).add(blockee);
        }
        else
        {
            // Create a new set to hold blocked users
            HashSet<String> blockedUsers = new HashSet<String>();
            // And add the current one
            blockedUsers.add(blockee);
            userBlocks.put(blocker, blockedUsers);
        }
    }

    public synchronized static void unblockFromTo(String blocker, String blockee)
    {
        // If the blocker has already blocked some people
        if (userBlocks.containsKey(blocker))
        {
            // Get that list of people
            Set<String> blockees = userBlocks.get(blocker);
            // If the blockee is in that list
            if (blockees.contains(blockee))
            {
                blockees.remove(blockee);
            }
        }
    }

    public synchronized static boolean userIsBlocked(String blocker, String blockee)
    {
        return userBlocks.get(blocker) != null && userBlocks.get(blocker).contains(blockee);
    }
}
