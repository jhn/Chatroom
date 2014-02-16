package mn.jhn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Validator
{
    private static final int MAX_LOGIN_ATTEMPS = 3;
    public static final Set<User> users;
    public static Set<String> loggedInUsers = Collections.synchronizedSet(new HashSet<String>());
    private static Set<InetAddress> blockedIps = Collections.synchronizedSet(new HashSet<InetAddress>());
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
        return !isUserAlreadyLoggedIn(user) && authenticatesSuccessfully(user);
    }

    public boolean isIpBlocked(Socket socket)
    {
        return blockedIps.contains(socket.getInetAddress());
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
