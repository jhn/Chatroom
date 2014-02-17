package mn.jhn;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Set;

public class Validator
{
    private static final int BLOCK_TIME = 60;
    public static final Set<User> users;

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

    public static int getBlockTime()
    {
        return BLOCK_TIME;
    }

    public static boolean isIpBlocked(Socket socket)
    {
        Date blockTime = Server.getBlockedIps().get(socket.getInetAddress());
        if (blockTime != null)
        {
            Date now = new Date();
            long seconds = (now.getTime() - blockTime.getTime()) / 1000;
            if (seconds >= 60)
            {
                Server.getBlockedIps().remove(socket.getInetAddress());
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean validateCredentials(String username, String password)
    {
        return !(username == null || username.isEmpty()
              || password == null || password.isEmpty());
    }

    public static boolean isLoggedIn(User user)
    {
        return Server.getLoggedInUsers().contains(user);
    }

    public static boolean authenticate(User user)
    {
        return users.contains(user);
    }

    public int getTotalUsers()
    {
        return users.size();
    }
}
