package mn.jhn.server;

import java.io.IOException;
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
    private static final Executor threadPool = Executors.newFixedThreadPool(10);
    private static final Set<User> loggedInUsers = Collections.synchronizedSet(new HashSet<User>());
    private static final Set<PrintWriter> writers = Collections.synchronizedSet(new HashSet<PrintWriter>());
    private static final Map<String, Map<InetAddress, Date>> blockedUsers =
            Collections.synchronizedMap(new HashMap<String, Map<InetAddress, Date>>());

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
        threadPool.execute(new ClientHandler(clientSocket));
    }

    public static Set<User> getLoggedInUsers()
    {
        return loggedInUsers;
    }

    public static Set<PrintWriter> getWriters()
    {
        return writers;
    }

    public static Map<String, Map<InetAddress, Date>> getBlockedUsers()
    {
        return blockedUsers;
    }

}
