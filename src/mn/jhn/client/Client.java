package mn.jhn.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Client
{
    private static final int TIME_OUT = 1800; // in seconds

    public static void main(String[] args) throws IOException
    {
        String host = args[0];
        int    port = Integer.parseInt(args[1]);

        final Socket         socket = new Socket(host, port);
        final PrintWriter    out    = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final BufferedReader stdIn  = new BufferedReader(new InputStreamReader(System.in));

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

        String userInput;
        try
        {
            while (true)
            {
                String socketInput = in.readLine();
                if (socketInput != null)
                {
                    System.out.println(socketInput);
                }
                else
                {
                    break;
                }

                // create timer for logging out innactive users
                ScheduledFuture<?> task = timer.schedule(new Interrupter(socket), TIME_OUT, TimeUnit.SECONDS);

                userInput = stdIn.readLine();

                // cancel timer if input read
                task.cancel(true);

                if (userInput != null)
                {
                    out.println(userInput);
                }
                else
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Cleaning up...");
            System.out.println(e);
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
            }
        }
    }

    private static class Interrupter implements Runnable
    {
        private final Socket socket;

        public Interrupter(Socket s)
        {
            this.socket = s;
        }

        @Override
        public void run()
        {
            try
            {
                System.out.println("Closing socket for inactivity.");
                socket.close();
                System.exit(0);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
