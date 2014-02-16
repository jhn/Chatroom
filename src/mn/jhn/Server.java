package mn.jhn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private final int port;
    private final Authenticator auth;
    private static HashSet<User> currentUsers = new HashSet<User>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static final Executor threadPool = Executors.newFixedThreadPool(10);

    public Server(int port)
    {
        this.port = port;
        this.auth = new Authenticator();
    }

    public void start() throws IOException
    {
        ServerSocket listener = new ServerSocket(this.port);
        System.out.println("Listening on port " + this.port);
        while (true)
        {
            spawnOnConnection(listener);
        }
    }

    private void spawnOnConnection(ServerSocket listener) throws IOException
    {
        final Socket clientSocket = listener.accept();
        threadPool.execute(new Runner(clientSocket));
    }

    private class Runner implements Runnable
    {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private User user;

        public Runner(Socket clientSocket) throws IOException
        {
            this.socket = clientSocket;
            this.in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out    = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run()
        {
            try
            {
                this.out.println("Username: ");
                String username = this.in.readLine();
                this.out.println("Password: ");
                String password = this.in.readLine();
                this.user = new User(username, password);

                if (username == null || password == null || !auth.authenticateUser(user))
                {
                    return;
                }

                currentUsers.add(user);
                writers.add(out);

                out.println("Logged in.");
                out.println("Welcome!");

                while (true)
                {
                    // todo: really necessary?
                    // todo: missing cleanup here
                    // todo: maybe we could add "this" to the currentUsers set?
                    // todo: or change the name to currentClient?
                    String message = in.readLine();
                    if (message == null)
                    {
                        return;
                    }
                    for (PrintWriter writer : writers)
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
                // todo: extract this to a method?
                currentUsers.remove(user);
                writers.remove(out);
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

        public void cleanUp()
        {

        }
    }
}
