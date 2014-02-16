package mn.jhn;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashSet;
import java.util.Set;

public class Authenticator
{
    private static final int MAX_LOGIN_ATTEMPS = 3;
    public final static Set<User> users;
    public static HashSet<String> loggedInUsers = new HashSet<String>();
    private static HashSet<Inet4Address> blockedIps = new HashSet<Inet4Address>();
    private int loginAttempts = 0;

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

    public boolean authenticateUser(User user)
    {
        return !isIpBlocked() && !isUserAlreadyLoggedIn(user) && authenticatesSuccessfully(user);
    }

    public boolean isIpBlocked()
    {
        return false;
    }

    public boolean isUserAlreadyLoggedIn(User user)
    {
        return loggedInUsers.contains(user.getUsername());
    }

    private boolean authenticatesSuccessfully(User user)
    {
        while (this.loginAttempts <= MAX_LOGIN_ATTEMPS)
        {
            if (users.contains(user))
            {
                // todo: remove on close
                loggedInUsers.add(user.getUsername());
                return true;
            }
            this.loginAttempts++;
        }
        return false;
    }

    public int getTotalUsers()
    {
        return users.size();
    }
}
