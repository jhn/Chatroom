package mn.jhn.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
            boolean authenticated = false;

            while (!authenticated)
            {
                /* Username authentication */

                this.out.println("Username: ");
                username = this.in.readLine();

                if (Validator.userExists(username))
                {
                    // Fill in our username->IP map field so we can use it later
                    this.usernameIpMap = new HashMap<String, InetAddress>(1);
                    this.usernameIpMap.put(username, this.socket.getInetAddress());

                    if (Validator.isUserBlockedForIp(username, this.socket.getInetAddress()))
                    {
                        this.out.println("User baned. Wait " + Validator.getBlockTime() + " seconds.");
                        continue;
                    }
                    if (Validator.isUserLoggedIn(username))
                    {
                        this.out.println("User is already logged in.");
                        continue;
                    }
                    // Initialize attempt counter for username->IP
                    loginAttempts.putIfAbsent(this.usernameIpMap, new AtomicInteger(0));
                }
                else
                {
                    this.out.println("Username not valid.");
                    continue;
                }

                /* Password authentication */

                this.out.println("Password: ");
                password = this.in.readLine();

                if (Validator.authenticate(username, password))
                {
                    this.user = new User(username, password);
                    authenticated = true;
                }
                else
                {
                    // Atomically increment the attempt counter
                    loginAttempts.get(this.usernameIpMap).getAndIncrement();
                    this.out.println("Wrong Password.");
                }

                // Get the AtomicInteger, then get the actual int
                if (loginAttempts.get(this.usernameIpMap).get() >= Validator.getMaxLoginAttemps())
                {
                    // Record ban time for IP and add it to the username list
                    // todo: synchronized block? it's 4 am; can't think very well
                    Map<InetAddress, Date> addressToDateMap = new HashMap<InetAddress, Date>();
                    addressToDateMap.put(this.socket.getInetAddress(), new Date());
                    Server.getBlockedUsers().put(username, addressToDateMap);
                    this.out.println("You have been banned for " + Validator.getBlockTime() + " seconds.");
                    // reset the attempt count
                    loginAttempts.get(this.usernameIpMap).getAndSet(0);
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

    private synchronized void wholasthr(String[] tokenizedInput)
    {

    }

    private synchronized void whoelse(String[] tokenizedInput)
    {
        
    }

    private synchronized void broadcast(String[] tokenizedInput)
    {
        for (PrintWriter writer : Server.getWriters())
        {
            writer.println(this.user.getUsername() + ": " + Arrays.toString(tokenizedInput));
        }
    }

    private synchronized void registerClient()
    {
        Server.getLoggedInUsers().add(this.user);
        Server.getWriters().add(this.out);
    }

    private synchronized void unregisterClient()
    {
        if (this.user != null)
        {
            Server.getLoggedInUsers().remove(this.user);
        }
        Server.getWriters().remove(this.out);
    }

}
