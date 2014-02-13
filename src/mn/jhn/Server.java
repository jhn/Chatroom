package mn.jhn;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server
{
    private int port;
    private HashSet<User> users;

    public void run() throws IOException
    {
        ServerSocket listener = new ServerSocket(9090);
        try
        {
            while (true)
            {
                Socket socket = listener.accept();
            }
        }
        finally
        {
            listener.close();
        }
    }
}
