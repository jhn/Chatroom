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
    private static final ConcurrentMap<Map<String, InetAddress>, AtomicInteger> LOGIN_ATTEMPTS_FOR_USER;
    private HashMap<String, InetAddress> usernameIpMap;
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private User user;

    // Map to hold attempts to log in
    static
    {
        LOGIN_ATTEMPTS_FOR_USER = new ConcurrentHashMap<Map<String, InetAddress>, AtomicInteger>();
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
            out.println(">Type 'help' to see available commands.");

            // for offline messages
            checkForPendingMessages(username);

            String userInput = in.readLine();
            while (userInput != null)
            {
                handleUserInput(userInput);
                userInput = in.readLine();
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception handled gracefully " + e);
        }
        finally
        {
            unregisterClient();
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
            LOGIN_ATTEMPTS_FOR_USER.putIfAbsent(this.usernameIpMap, new AtomicInteger(0));
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
            LOGIN_ATTEMPTS_FOR_USER.get(this.usernameIpMap).getAndIncrement();
            this.out.println(">Wrong Password.");
            return false;
        }
    }

    private boolean userHasNoMoreAttempts()
    {
        return LOGIN_ATTEMPTS_FOR_USER.get(this.usernameIpMap).get() >= Validator.getMaxLoginAttemps();
    }

    private void banCurrentUser(String username)
    {
        // Record ban time for IP and add it to the username list
        Map<InetAddress, Date> addressToDateMap = new HashMap<InetAddress, Date>();
        addressToDateMap.put(this.socket.getInetAddress(), new Date());
        Auditor.getServerBlocks().put(username, addressToDateMap);

        // reset the attempt count
        LOGIN_ATTEMPTS_FOR_USER.get(this.usernameIpMap).getAndSet(0);

        this.out.println(">You have been banned for " + Validator.getBlockTime() + " seconds.");
    }

    private void handleUserInput(String userInput)
    {
        // tokenize the input so we can better handle it
        String[] tokenizedInput = userInput.split("\\s+");
        String userCommand = tokenizedInput[0];

        // gets the command from the Command enum
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
            case WEATHER:   weather(tokenizedInput);   break;
            case MYIP:      myip(tokenizedInput);      break;
            case AWAY:      away(tokenizedInput);      break;
            case BACK:      back(tokenizedInput);      break;
            case STATUSES:  statuses(tokenizedInput);  break;
            case HELP:      help(tokenizedInput);      break;
        }
    }

    private void notSupported()
    {
        this.out.println(">Command not supported");
    }

    private void logout(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.LOGOUT, argLength))
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            this.out.println(">Bye!");
            unregisterClient();
        }
    }

    private void unblock(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.UNBLOCK, argLength))
        {
            this.out.println(">Usage: unblock <user>");
            return;
        }

        String targetUser = tokenizedInput[1];

        if (targetUser.equals(this.user.getUsername()))
        {
            this.out.println(">You can't unblock yourself!");
            return;
        }

        if(Validator.userExists(targetUser))
        {
            // User already blocked
            if (Auditor.userIsBlocked(this.user.getUsername(), targetUser))
            {
                Auditor.unblockFromTo(this.user.getUsername(), targetUser);
                this.out.println(">You have unblocked " + targetUser);
            }
            else
            {
                this.out.println("> " + targetUser + " not blocked");
            }
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    private void block(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.BLOCK, argLength))
        {
            this.out.println(">Syntax: block <user>");
            return;
        }

        String targetUser = tokenizedInput[1];

        if (targetUser.equals(this.user.getUsername()))
        {
            this.out.println(">You can't block yourself!");
            return;
        }

        if(Validator.userExists(targetUser))
        {
            // User already blocked
            if (Auditor.userIsBlocked(this.user.getUsername(), targetUser))
            {
                this.out.println("> " + targetUser + " is already blocked");
            }
            else
            {
                Auditor.blockFromTo(this.user.getUsername(), targetUser);
                this.out.println(">You have blocked " + targetUser + " from sending you messages");
            }
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    private void message(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (argLength < Command.MESSAGE.getArity())
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
                String[] message = Arrays.copyOfRange(tokenizedInput, 2, argLength + 1);
                String messageString = Utils.join(message);

                // Either the user is online and we send, or is offline and we queue
                if (Auditor.getLoggedInUsers().containsKey(targetUser))
                {
                    // Let's see if the user is away and if so display the message they set up
                    if  (Auditor.getUserStatus(targetUser) == Auditor.UserStatus.AWAY)
                    {
                        this.out.println(Auditor.getAwayMessageForUser(targetUser));
                    }
                    PrintWriter targetUserWriter = Auditor.getLoggedInUsers().get(targetUser);
                    targetUserWriter.println(">" + this.user.getUsername() + ": " + messageString);
                }
                else
                {
                    MessageQueue.addOfflineMessage(targetUser, this.user.getUsername(), messageString);
                    this.out.println(">User offline. Message queued.");
                }
            }
            else
            {
                this.out.println("> You cannot send any message to " + targetUser + ".  You have been blocked by the user.");
            }
        }
        else
        {
            this.out.println(">User " + targetUser + " does not exist");
        }
    }

    private void wholasthr(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.WHOLASTHR, argLength))
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            this.out.println(">Users in the last hour: ");
            Date now = new Date();
            Set<String> usersInLastHour = new HashSet<String>();
            // logged out users within LAST_HOUR
            for (Map.Entry<String, Date> entry : Auditor.getLoggedOutUsers().entrySet())
            {
                if ((now.getTime() - entry.getValue().getTime()) / 1000 % 60 < Validator.getLastHour())
                {
                    usersInLastHour.add(entry.getKey());
                }
            }
            // plus all logged in usernames
            usersInLastHour.addAll(Auditor.getLoggedInUsernames());
            // minus the current user
            usersInLastHour.remove(user.getUsername());
            for (String u : usersInLastHour)
            {
                this.out.println(u);
            }
        }
    }

    private void whoelse(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.WHOELSE, argLength))
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
        // broadcast to all available user writers
        for (PrintWriter writer : writers)
        {
            writer.println(">" + this.user.getUsername() + ": " + messageString);
        }
    }

    // gets the weather for a city!
    private void weather(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.WEATHER, argLength))
        {
            this.out.println(">Usage: weather <city>");
        }
        else
        {
            HTTPCall call = new HTTPCall();
            String city = tokenizedInput[1];
            String weatherEndpoint = call.getWeatherEndpoint() + city;
            try
            {
                String response = call.makeCall(weatherEndpoint);
                this.out.println(">Weather for " + city);
                this.out.println(response);
            }
            catch (Exception e)
            {
            }
        }
    }

    // displays the user's ip
    private void myip(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.MYIP, argLength))
        {
            this.out.println(">This command does not take any arguments");
        }
        else
        {
            HTTPCall call = new HTTPCall();
            try
            {
                String response = call.makeCall(call.getIpEndpoint());
                this.out.println(">Your ip: ");
                this.out.println(response);
            }
            catch (Exception e)
            {
            }
        }
    }

    // sets the user as away
    private void away(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        String messageString;
        if (argLength > 1)
        {
            String[] message = Arrays.copyOfRange(tokenizedInput, 1, argLength + 1);
            messageString = ">" + this.user.getUsername() + " is away. Auto-response: " + Utils.join(message);
        }
        else
        {
            messageString = ">Message delivered, but this user is currently away.";
        }

        Auditor.setUserStatus(this.user.getUsername(), Auditor.UserStatus.AWAY);
        Auditor.setAwayMessageForUser(this.user.getUsername(), messageString);
        this.out.println(">Your status is now set as AWAY.");
    }

    // sets the user from away to online
    private void back(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.WHOELSE, argLength))
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            if (Auditor.getUserStatus(this.user.getUsername()) == Auditor.UserStatus.AWAY)
            {
                Auditor.setUserStatus(this.user.getUsername(), Auditor.UserStatus.ONLINE);
                this.out.println(">Your status is now set as ONLINE.");
            }
            else
            {
                this.out.println(">Your status was not set as AWAY, so nothing happened.");
            }
        }
    }

    // display logged users with their statuses
    private void statuses(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.STATUSES, argLength))
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            for (Map.Entry<String, String> c : Auditor.getUsersAndStatuses().entrySet())
            {
                this.out.println(">" + c.getKey() + " - " + c.getValue());
            }
        }
    }

    // Displays all available commands
    private void help(String[] tokenizedInput)
    {
        int argLength = tokenizedInput.length - 1;
        if (!Command.checkArityForCommand(Command.HELP, argLength))
        {
            this.out.println(">This command takes no arguments");
        }
        else
        {
            for (Map.Entry<String, String> c : Command.getCommandsWithDescriptions().entrySet())
            {
                this.out.println("> " + c.getKey() + ": " + c.getValue());
            }
        }
    }

    // prints out pending messages for the user, if any
    private void checkForPendingMessages(String username)
    {
        Map<String, List<String>> pendingMessages = MessageQueue.pendingMessagesForUser(username);
        if (pendingMessages != null)
        {
            for(Map.Entry<String, List<String>> messagesFromSender: pendingMessages.entrySet())
            {
                String sender = messagesFromSender.getKey();
                List<String> messages = messagesFromSender.getValue();
                for (String message : messages)
                {
                    this.out.println(">" + sender + ": " + message);
                }
            }
        }
        MessageQueue.emptyInboxForUser(username);
    }

    // registers streams and the current user
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
