package mn.jhn.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
    public static void main(String[] args) throws IOException
    {
        String host = args[0];
        int    port = Integer.parseInt(args[1]);

        final Socket         socket = new Socket(host, port);
        final PrintWriter    out    = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedReader stdIn  = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        try
        {
            while (true)
            {
                userInput = stdIn.readLine();
                if (userInput != null)
                {
                    out.println(userInput);
                    String socketInput = in.readLine();
                    if (socketInput == null)
                    {
                        break;
                    }
                    System.out.println(socketInput);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Cleaning up...");
        }
        finally
        {
            socket.close();
        }
    }
}
