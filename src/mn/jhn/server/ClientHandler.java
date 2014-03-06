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
            String username = "";
            String password;
            boolean validUsername;
            boolean validUsernamePassword = false;

            while (!validUsernamePassword)
            {
                /* Username authentication */

                validUsername = false;
                while (!validUsername)
                {
                    this.out.println(">Username: ");
                    username = this.in.readLine();
                    validUsername = validateUsername(username);
                }

                /* Username-Password authentication */

                this.out.println(">Password: ");
                password = this.in.readLine();

                validUsernamePassword = validatePassword(username, password);

                if (userHasNoMoreAttempts())
                {
                    banCurrentUser(username);
                }
            }

            registerClient();

            out.println(">Logged in.");
            out.println(">Welcome!");

            checkForPendingMessages(username);

            String userInput = in.readLine();
            while (true)
            {
                handleUserInput(userInput);
                userInput = in.readLine();
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception handled gracefully " + e);
            e.printStackTrace();
        }
        finally
        {
            unregisterClient();
        }
    }

    private void checkForPendingMessages(String username)
    {
        Map<String, List<String>> pendingMessages = MessageQueue.pendingMessagesForUser(username);
        if (pendingMessages != null)
        {
            for(Map.Entry<String, List<String>> entry: pendingMessages.entrySet()) {
                String sender = entry.getKey();
                List<String> messages = entry.getValue();
                for (String message : messages)
                {
                    this.out.println(">" + sender + ": " + message);
                }
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
                this.out.println(">User baned. Wait " + Validator.getBlockTime() + " seconds.");
                return false;
            }
            if (Validator.isUserLoggedIn(username))
            {
                this.out.println(">User is already logged in.");
                return false;
            }
            // Initialize attempt counter for username->IP
            loginAttempts.putIfAbsent(this.usernameIpMap, new AtomicInteger(0));
        }
        else
        {
            this.out.println(">Username not valid.");
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
            this.out.println(">Wrong Password.");
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
        Map<InetAddress, Date> addressToDateMap = new HashMap<InetAddress, Date>();
        addressToDateMap.put(this.socket.getInetAddress(), new Date());
        Auditor.getServerBlocks().put(username, addressToDateMap);

        // reset the attempt count
        loginAttempts.get(this.usernameIpMap).getAndSet(0);

        this.out.println(">You have been banned for " + Validator.getBlockTime() + " seconds.");
    }

    private void handleUserInput(String userInput)
    {
        if (userInput == null)
        {
            this.out.println(">Null command.");
            return;
        }

        String[] tokenizedInput = userInput.split("\\s+");
        String userCommand = tokenizedInput[0];

        dispatchCommand(Command.getCommand(userCommand), tokenizedInput);
    }

    private void dispatchCommand(Command command, String[] tokenizedInput)
    {
        if (command == null)
        {
            notSupported();
            return;
        }
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

    private void notSupported()
    {
        this.out.println(">Command not supported");
    }

    private void logout(String[] tokenizedInput)
    {
        if (tokenizedInput.length != 1)
        {
            this.out.println(">This commands takes no arguments");
        }
        else
        {
            this.out.println(">Bye!");
            unregisterClient();
        }
    }

    private void unblock(String[] tokenizedInput)
    {
        if (tokenizedInput.length != 2)
        {
            this.out.println(">Usage: unblock <user>");
        }

        String targetUser = tokenizedInput[1];

        if(Validator.userExists(targetUser))
        {
            Auditor.unblockFromTo(this.user.getUsername(), targetUser);
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    private void block(String[] tokenizedInput)
    {
        if (tokenizedInput.length != 2)
        {
            this.out.println(">Syntax: block <user>");
        }
        String targetUser = tokenizedInput[1];
        if(Validator.userExists(targetUser))
        {
            Auditor.blockFromTo(this.user.getUsername(), targetUser);
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    private void message(String[] tokenizedInput)
    {
        if (tokenizedInput.length < 3)
        {
            this.out.println(">Usage: message <user> <message>");
            return;
        }

        String targetUser = tokenizedInput[1];
        if (targetUser.equals(this.user.getUsername()))
        {
            this.out.println(">You can't message yourself.");
            return;
        }

        if(Validator.userExists(targetUser))
        {
            if (!Auditor.userIsBlocked(targetUser, this.user.getUsername()))
            {
                // Save the message
                String[] message = Arrays.copyOfRange(tokenizedInput, 2, tokenizedInput.length);
                String messageString = Utils.join(message);

                // Either the user is online and we send, or is offline and we queue
                if (Auditor.getLoggedInUsers().containsKey(targetUser))
                {
                    PrintWriter userWriter = Auditor.getLoggedInUsers().get(targetUser);
                    userWriter.println(">" + this.user.getUsername() + ": " + messageString);
                }
                else
                {
                    MessageQueue.addOfflineMessage(targetUser, this.user.getUsername(), messageString);
                    this.out.println(">User offline. Message queued.");
                }
            }
            else
            {
                this.out.println(">" + targetUser + " has blocked you.");
            }
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    // todo: make sure this is actually what they want
    private void wholasthr(String[] tokenizedInput)
    {
        if (tokenizedInput.length != 1)
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            this.out.println(">Users in the last hour: ");
            Date now = new Date();
            Set<String> usersInLastHour = new HashSet<String>();
            for (Map.Entry<String, Date> entry : Auditor.getLoggedOutUsers().entrySet())
            {
                if ((now.getTime() - entry.getValue().getTime()) / (60 * 1000) % 60 < 60)
                {
                    usersInLastHour.add(entry.getKey());
                }
            }
            usersInLastHour.addAll(Auditor.getLoggedInUsernames());
            usersInLastHour.remove(user.getUsername());
            for (String u : usersInLastHour)
            {
                this.out.println(u);
            }
        }
    }

    private void whoelse(String[] tokenizedInput)
    {
        if (tokenizedInput.length != 1)
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            this.out.println(">Logged in users:");
            Set<String> loggedInUsers = Auditor.getLoggedInUsernames();
            loggedInUsers.remove(this.user.getUsername());
            for (String username : loggedInUsers)
            {
                this.out.println(username);
            }
        }
    }

    private void broadcast(String[] tokenizedInput)
    {
        Set<PrintWriter> writers = Auditor.getWriters();
        writers.remove(this.out);
        // Save the message
        String[] message = Arrays.copyOfRange(tokenizedInput, 1, tokenizedInput.length);
        String messageString = Utils.join(message);
        for (PrintWriter writer : writers)
        {
            writer.println(">" + this.user.getUsername() + ": " + messageString);
        }
    }

    private void registerClient()
    {
        Auditor.registerClient(this.user, this.out);
    }

    private void unregisterClient()
    {
        Auditor.unregisterClient(this.user);
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
        }
    }
}
