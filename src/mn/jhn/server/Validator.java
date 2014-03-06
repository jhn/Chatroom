package mn.jhn.server;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Validator
{
    private static final int BLOCK_TIME = 10;
    private static final int MAX_LOGIN_ATTEMPS = 3;

    public static boolean isUserBlockedForIp(String username, InetAddress ip)
    {
        Map<InetAddress, Date> ipToDateMap = Auditor.getServerBlocks().get(username);

        if (ipToDateMap != null)
        {
            // There's at least one IP
            Date blockTime = ipToDateMap.get(ip);
            if (blockTime != null)
            {
//                IP has a block time
                Date now = new Date();
                long diff = now.getTime() - blockTime.getTime();
                long seconds = diff / 1000 % 60;
                if (seconds >= BLOCK_TIME)
                {
//                    todo: probably should be synchronized; it's 4 am, fix me
                    // Let's eliminate the ip->date map from the collection
                    Auditor.getServerBlocks().get(username).remove(ip);
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
        return isUsernameInCollection(Auditor.getUsers(), username);
    }

    public static boolean isUserLoggedIn(String username)
    {
        return Auditor.getLoggedInUsers().contains(username);
    }

    public static boolean userIsLoggedIn(User user)
    {
        return Auditor.getLoggedInUsers().contains(user.getUsername());
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
        return validateCredentials(username, password) && Auditor.getUsers().contains(new User(username, password));
    }

    public static boolean validateCredentials(String username, String password)
    {
        return !(username == null || username.isEmpty() || password == null || password.isEmpty());
    }

    public static int getBlockTime()
    {
        return BLOCK_TIME;
    }

    public static int getMaxLoginAttemps()
    {
        return MAX_LOGIN_ATTEMPS;
    }

}
