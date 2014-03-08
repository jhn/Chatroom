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
    private static final Map<String, UserStatus> userStatus;
    private static final Map<String, String> awayMessages;

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
        userStatus      = Collections.synchronizedMap(new HashMap<String, UserStatus>());
        awayMessages    = Collections.synchronizedMap(new HashMap<String, String>());
        initializeUserStatus();
    }

    private static void initializeUserStatus()
    {
        for (String s : getUsernames())
        {
            userStatus.put(s, UserStatus.OFFLINE);
        }
    }

    public synchronized static Set<User> getUsers()
    {
        return new HashSet<User>(users);
    }

    public synchronized static Set<String> getUsernames()
    {
        Set<String> usernames = new HashSet<String>();
        for (User user : users)
        {
            usernames.add(user.getUsername());
        }
        return usernames;
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

    public static UserStatus getUserStatus(String username)
    {
        return userStatus.get(username);
    }

    public synchronized static void setUserStatus(String username, UserStatus status)
    {
        userStatus.put(username, status);
    }

    public synchronized static void registerClient(User user, PrintWriter out)
    {
        loggedInUsers.put(user.getUsername(), out);
        userStatus.put(user.getUsername(), UserStatus.ONLINE);
    }

    public synchronized static void unregisterClient(User user)
    {
        if (user != null)
        {
            loggedInUsers.remove(user.getUsername());
            loggedOutUsers.put(user.getUsername(), new Date());
            userStatus.put(user.getUsername(), UserStatus.OFFLINE);
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

    public static String getAwayMessageForUser(String username)
    {
        return awayMessages.get(username);
    }

    public synchronized static void setAwayMessageForUser(String username, String message)
    {
        awayMessages.put(username, message);
    }

    protected enum UserStatus
    {
        ONLINE("online"),
        OFFLINE("offline"),
        AWAY("away");

        private final String name;

        UserStatus(String name)
        {
            this.name = name;
        }

        private String getStatusNames()
        {
            return this.name;
        }
    }

    public synchronized static Map<String, String> getUsersAndStatuses()
    {
        Map<String, String> userToStatusMap = new HashMap<String, String>();
        for (String username : loggedInUsers.keySet())
        {
            userToStatusMap.put(username, getUserStatus(username).getStatusNames());
        }
        return userToStatusMap;
    }
}
