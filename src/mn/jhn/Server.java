package mn.jhn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private final int port;
    private static final Set<User> loggedInUsers = Collections.synchronizedSet(new HashSet<User>());
    private static final Map<InetAddress, Date> blockedIps = Collections.synchronizedMap(new HashMap<InetAddress, Date>());
    private static final Set<PrintWriter> currentWriters = Collections.synchronizedSet(new HashSet<PrintWriter>());
    private static final Executor threadPool = Executors.newFixedThreadPool(10);

    public Server(int port)
    {
        this.port = port;
    }

    public void start() throws IOException
    {
        ServerSocket listener = new ServerSocket(this.port);
        System.out.println("Listening on port " + this.port);
        while (true)
        {
            spawnThreadOnConnection(listener);
        }
    }

    private void spawnThreadOnConnection(ServerSocket listener) throws IOException
    {
        final Socket clientSocket = listener.accept();
        if (Validator.isIpBlocked(clientSocket))
        {
            clientSocket.close();
        }
        else
        {
            threadPool.execute(new Runner(clientSocket));
        }
    }

    public static Set<User> getLoggedInUsers()
    {
        return loggedInUsers;
    }

    public static Map<InetAddress, Date> getBlockedIps()
    {
        return blockedIps;
    }

    private class Runner implements Runnable
    {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private User user;

        public Runner(Socket clientSocket) throws IOException
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
                User user;
                String username = "";
                String password = "";
                int attempts = 3;
                while (attempts > 0)
                {
                    this.out.println("Username: ");
                    username = this.in.readLine();
                    this.out.println("Password: ");
                    password = this.in.readLine();

                    if (!Validator.validateCredentials(username, password))
                    {
                        attempts--;
                        this.out.println("Please enter a valid username / password.");
                        this.out.println("You have " + attempts + " attempts remaining.");
                    }
                    else
                    {
                        user = new User(username, password);
                        if (Validator.isLoggedIn(user))
                        {
                            this.out.println("This user is already logged in.");
                            return;
                        }
                        if (Validator.authenticate(user))
                        {
                            this.user = user;
                            break;
                        }
                        else
                        {
                            attempts--;
                            this.out.println("Wrong username / password.");
                            this.out.println("You have " + attempts + " attempts remaining.");
                        }
                    }
                }

                if (attempts == 0)
                {
                    Server.getBlockedIps().put(this.socket.getInetAddress(), new Date());
                    this.out.println("You have been banned for " + Validator.getBlockTime() + " seconds.");
                    return;
                }

                registerClient();

                out.println("Logged in.");
                out.println("Welcome!");

                while (true)
                {
                    String message = in.readLine();
                    if (message == null)
                    {
                        return;
                    }
                    for (PrintWriter writer : currentWriters)
                    {
                        writer.println(username + ": " + message);
                    }
                }
            }
            catch (IOException e)
            {
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

        private void registerClient()
        {
            loggedInUsers.add(this.user);
            currentWriters.add(this.out);
        }

        private void unregisterClient()
        {
            if (this.user != null)
            {
                loggedInUsers.remove(this.user);
            }
            currentWriters.remove(this.out);
        }
    }
}
