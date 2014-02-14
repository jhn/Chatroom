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

    public boolean authenticateUser(String username, String password)
    {
        return !isIpBlocked() && !isUserAlreadyLoggedIn(username) && authenticatesSuccessfully(username, password);
    }

    public boolean isIpBlocked()
    {
        return false;
    }

    public boolean isUserAlreadyLoggedIn(String username)
    {
        return loggedInUsers.contains(username);
    }

    private boolean authenticatesSuccessfully(String username, String password)
    {
        while (this.loginAttempts <= MAX_LOGIN_ATTEMPS)
        {
            if (users.contains(new User(username, password)))
            {
                // todo: remove on close
                loggedInUsers.add(username);
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
