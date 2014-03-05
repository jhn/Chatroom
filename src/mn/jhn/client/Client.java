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
        int port    = Integer.parseInt(args[1]);

        final Socket socket = new Socket(host, port);
        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // To finish gracefully on interrupt signal
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
                System.out.println("Trying to unregister");
                out.println("logout");
            }
        });

        String userInput;
        while (true)
        {
            userInput = stdIn.readLine();
            if (userInput != null)
            {
                out.println(userInput);
                System.out.println(in.readLine());
            }
        }
    }
}
