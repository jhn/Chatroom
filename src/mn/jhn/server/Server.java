package mn.jhn.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server
{
    private final int port;
    private static final Executor THREAD_POOL = Executors.newFixedThreadPool(15);

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
            final Socket clientSocket = listener.accept();
            THREAD_POOL.execute(new ClientHandler(clientSocket));
        }
    }
}
