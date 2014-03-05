package mn.jhn.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable
{
    private static final ConcurrentMap<Map<String, InetAddress>, AtomicInteger> loginAttempts;
    private HashMap<String, InetAddress> usernameIpMap;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private User user;

    static
    {
        loginAttempts = new ConcurrentHashMap<Map<String, InetAddress>, AtomicInteger>();
    }

    public ClientHandler(Socket clientSocket) throws IOException
    {
        this.socket    = clientSocket;
        this.in        = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out       = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run()
    {
        try
        {
            String username;
            String password;
            boolean validUsername = false;
            boolean validUsernamePassword = false;

            while (!validUsernamePassword)
            {
                /* Username authentication */

                this.out.println("Username: ");
                username = this.in.readLine();

                while (!validUsername)
                {
                    validUsername = validateUsername(username);
                }

                /* Username-Password authentication */

                this.out.println("Password: ");
                password = this.in.readLine();

                validUsernamePassword = validatePassword(username, password);

                if (userHasNoMoreAttempts())
                {
                    banCurrentUser(username);
                }
            }

            registerClient();

            out.println("Logged in.");
            out.println("Welcome!");

            String userInput = in.readLine();
            while (!"logout".equals(userInput))
            {
                handleUserInput(userInput);
                userInput = in.readLine();
            }
        }
        catch (Exception e)
        {
            System.out.println("A thing happened.");
            e.printStackTrace();
        }
        finally
        {
            unregisterClient();
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                System.out.println("Couldn't close the socket: " + e);
            }
        }
    }

    private boolean validateUsername(String username)
    {
        if (Validator.userExists(username))
        {
            // Fill in our username->IP map field so we can use it later
            this.usernameIpMap = new HashMap<String, InetAddress>(1);
            this.usernameIpMap.put(username, this.socket.getInetAddress());

            if (Validator.isUserBlockedForIp(username, this.socket.getInetAddress()))
            {
                this.out.println("User baned. Wait " + Validator.getBlockTime() + " seconds.");
                return false;
            }
            if (Validator.isUserLoggedIn(username))
            {
                this.out.println("User is already logged in.");
                return false;
            }
            // Initialize attempt counter for username->IP
            loginAttempts.putIfAbsent(this.usernameIpMap, new AtomicInteger(0));
        }
        else
        {
            this.out.println("Username not valid.");
            return false;
        }
        return true;
    }

    private boolean validatePassword(String username, String password)
    {
        if (Validator.authenticate(username, password))
        {
            this.user = new User(username, password);
            return true;
        }
        else
        {
            // Atomically increment the attempt counter
            loginAttempts.get(this.usernameIpMap).getAndIncrement();
            this.out.println("Wrong Password.");
            return false;
        }
    }

    private boolean userHasNoMoreAttempts()
    {
        return loginAttempts.get(this.usernameIpMap).get() >= Validator.getMaxLoginAttemps();
    }

    private void banCurrentUser(String username)
    {
        // Record ban time for IP and add it to the username list
        // todo: synchronized block? it's 4 am; can't think very well
        Map<InetAddress, Date> addressToDateMap = new HashMap<InetAddress, Date>();
        addressToDateMap.put(this.socket.getInetAddress(), new Date());
        Auditor.getBlockedUsers().put(username, addressToDateMap);
        this.out.println("You have been banned for " + Validator.getBlockTime() + " seconds.");
        // reset the attempt count
        loginAttempts.get(this.usernameIpMap).getAndSet(0);
    }

    private void handleUserInput(String userInput)
    {
        if (userInput == null)
        {
            this.out.println("Null command.");
            return;
        }

        String[] tokenizedInput = userInput.split("\\s+");
        String userCommand = tokenizedInput[0];

        if (!Command.getCommands().contains(userCommand))
        {
            this.out.println("Command not supported.");
            return;
        }

        dispatchCommand(Command.getCommand(userCommand), tokenizedInput);
    }

    private void dispatchCommand(Command command, String[] tokenizedInput)
    {
        switch (command)
        {
            case WHOELSE:   whoelse(tokenizedInput);   break;
            case WHOLASTHR: wholasthr(tokenizedInput); break;
            case BROADCAST: broadcast(tokenizedInput); break;
            case MESSAGE:   message(tokenizedInput);   break;
            case BLOCK:     block(tokenizedInput);     break;
            case UNBLOCK:   unblock(tokenizedInput);   break;
            case LOGOUT:    logout(tokenizedInput);    break;
        }
    }

    private void logout(String[] tokenizedInput)
    {
        this.out.println("Bye!");
        unregisterClient();
    }

    private void unblock(String[] tokenizedInput)
    {
        this.out.println("Should unblock.");
    }

    private void block(String[] tokenizedInput)
    {
        this.out.println("Should block.");
    }

    // todo: should be able to send message to non-logged in users
    private void message(String[] tokenizedInput)
    {
        this.out.println("Should send message.");
    }

    // todo: should not display current user
    private synchronized void wholasthr(String[] tokenizedInput)
    {
        this.out.println("Users in the last hour: ");
        Date now = new Date();
        for (Map.Entry<String, Date> entry : Auditor.getLoggedOutUsers().entrySet())
        {
            if (now.getTime() - entry.getValue().getTime() / (60 * 1000) % 60 < 60)
            {
                this.out.println(entry.getKey());
            }
        }
    }

    // todo: validation on tokenizedInput
    // todo: should not display current user
    private synchronized void whoelse(String[] tokenizedInput)
    {
        for (User u : Auditor.getLoggedInUsers())
        {
            this.out.println("Logged in users: ");
            this.out.println(u.getUsername());
        }
    }

    // todo: create a join method for the array
    private synchronized void broadcast(String[] tokenizedInput)
    {
        for (PrintWriter writer : Auditor.getWriters())
        {
            writer.println(this.user.getUsername() + ": " + Arrays.toString(tokenizedInput));
        }
    }

    private synchronized void registerClient()
    {
        Auditor.registerClient(this.user, this.out);
    }

    private synchronized void unregisterClient()
    {
        Auditor.unregisterClient(this.user, this.out);
    }
}
