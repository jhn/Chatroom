package mn.jhn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private final int port;
    private final Authenticator auth;
    private Set<User> currentUsers;
    private Executor threadPool;

    public Server(int port)
    {
        this.port = port;
        this.auth = new Authenticator();
        this.currentUsers = new HashSet<User>();
    }

    public void start() throws IOException
    {
        this.threadPool = Executors.newFixedThreadPool(this.auth.getTotalUsers());
        ServerSocket listener = new ServerSocket(this.port);
        while (true)
        {
            spawnOnConnection(listener);
        }
    }

    private void spawnOnConnection(ServerSocket listener) throws IOException
    {
        final Socket clientSocket = listener.accept();
        this.threadPool.execute(new Runner(clientSocket));
    }

    private class Runner implements Runnable
    {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Runner(Socket clientSocket)
        {
            this.socket = clientSocket;
        }

        @Override
        public void run()
        {
            try
            {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new PrintWriter(socket.getOutputStream(), true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
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
    }
}
