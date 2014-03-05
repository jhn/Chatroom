package mn.jhn.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Validator
{
    private static final int BLOCK_TIME = 10;
    private static final int MAX_LOGIN_ATTEMPS = 3;
    private static final Set<User> users;

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

    public static boolean isUserBlockedForIp(String username, InetAddress ip)
    {
        Map<InetAddress, Date> ipToDateMap = Server.getBlockedUsers().get(username);

        if (ipToDateMap != null)
        {
            // There's at least one IP
            Date blockTime = ipToDateMap.get(ip);
            if (blockTime != null)
            {
//                IP has a block time
                Date now = new Date();
                long seconds = (now.getTime() - blockTime.getTime()) / 1000;
                if (seconds >= BLOCK_TIME)
                {
//                    todo: probably should be synchronized; it's 4 am, fix me
                    // Let's eliminate the ip->date map from the collection
                    Server.getBlockedUsers().get(username).remove(ip);
                    return false;
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: precompute all userNames and throw them into a set for fast retrieval
    public static boolean userExists(String username)
    {
        return isUsernameInCollection(users, username);
    }

    public static boolean isUserLoggedIn(String username)
    {
        return isUsernameInCollection(Server.getLoggedInUsers(), username);
    }

    public static boolean userIsLoggedIn(User user)
    {
        return Server.getLoggedInUsers().contains(user);
    }

    private static boolean isUsernameInCollection(Collection<User> c, String username)
    {
        if (username == null || username.isEmpty())
        {
            return false;
        }

        for (User user : c)
        {
            if (user.getUsername().equals(username))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean authenticate(String username, String password)
    {
        return validateCredentials(username, password) && users.contains(new User(username, password));
    }

    public static boolean validateCredentials(String username, String password)
    {
        return !(username == null || username.isEmpty() || password == null || password.isEmpty());
    }

    public static int getTotalUsers()
    {
        return users.size();
    }

    public static int getBlockTime()
    {
        return BLOCK_TIME;
    }

    public static int getMaxLoginAttemps()
    {
        return MAX_LOGIN_ATTEMPS;
    }

    public static Set<User> getUsers()
    {
        return users;
    }
}
