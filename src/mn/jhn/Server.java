package mn.jhn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server
{
    private final int port;
    private HashSet<User> users;

    public Server(int port)
    {
        this.port = port;
    }

    public void start() throws IOException
    {
        ServerSocket listener = new ServerSocket(this.port);
        try
        {
            while (true)
            {
                new Runner(listener.accept()).run();
            }
        }
        finally
        {
            listener.close();
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
