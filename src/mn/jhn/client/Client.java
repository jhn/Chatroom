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

        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

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
