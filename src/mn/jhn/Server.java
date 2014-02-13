package mn.jhn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private final int port;
    private Set<User> users;
    private Executor pool;

    public Server(int port)
    {
        this.port = port;
    }

    public void registerUsers(Set<User> users)
    {
        this.users = new HashSet<User>(users);
    }

    public void start() throws IOException
    {
        if (this.users == null || users.isEmpty())
        {
            throw new RuntimeException("No registered users.");
        }

        this.pool = Executors.newFixedThreadPool(this.users.size());
        ServerSocket listener = new ServerSocket(this.port);
        spawnOnConnection(listener);
    }

    private void spawnOnConnection(ServerSocket listener) throws IOException
    {
        while (true)
        {
            final Socket s = listener.accept();
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    // magic
                }
            };
            this.pool.execute(r);
        }
    }

    private class Runner extends Thread
    {
        private String name;
        private Socket socket;

        public Runner(Socket socket)
        {
            this.socket = socket;
        }

        public void run()
        {
            System.out.println("Running!");
        }
    }
}
