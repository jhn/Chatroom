package mn.jhn.server;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Validator
{
    private static final int BLOCK_TIME = 60;
    private static final int MAX_LOGIN_ATTEMPS = 3;
    private static final int LAST_HOUR = 3600; // in seconds
    private static final int TIME_OUT = 1800; // in seconds

    public static boolean isUserBlockedForIp(String username, InetAddress ip)
    {
        Map<InetAddress, Date> ipToDateMap = Auditor.getServerBlocks().get(username);

        if (ipToDateMap != null)
        {
            // There's at least one IP
            Date blockTime = ipToDateMap.get(ip);
            if (blockTime != null)
            {
                // IP has a block time
                Date now = new Date();
                long diff = now.getTime() - blockTime.getTime();
                long seconds = diff / 1000 % 60;
                if (seconds >= BLOCK_TIME)
                {
                    // Unblock it
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

    public static boolean userExists(String username)
    {
        return isUsernameInCollection(Auditor.getUsers(), username);
    }

    public static boolean isUserLoggedIn(String username)
    {
        return Auditor.getLoggedInUsers().containsKey(username);
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

    public static int getLastHour()
    {
        return LAST_HOUR;
    }

    public static int getTimeOut()
    {
        return TIME_OUT;
    }
}
