package mn.jhn.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.*;

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

        // executor that executes a task after a given time
        ScheduledExecutorService timer      = Executors.newSingleThreadScheduledExecutor();
        // executor for handling input from the server and output to the client
        ExecutorService serverInputExecutor = Executors.newSingleThreadExecutor();

        String userInput;
        try
        {
            while (true)
            {
                // Initializes the server handler thread
                serverInputExecutor.execute(new ServerHandler(in));

                // Future that promises to run the task after TIME_OUT seconds...
                ScheduledFuture<?> task = timer.schedule(
                        new Terminator(socket, out),
                        TIME_OUT,
                        TimeUnit.SECONDS
                );

                userInput = stdIn.readLine();

                // ...unless the user inputs something!
                task.cancel(true);

                if (userInput != null && !socket.isClosed())
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
            System.out.println("Terminating gracefully...");
            System.out.println(e);
            e.printStackTrace();
            System.out.println("Done!");
        }
        finally
        {
            try
            {
                if (!socket.isClosed())
                {
                    socket.close();
                }
                if (!timer.isShutdown())
                {
                    timer.shutdownNow();
                }
                if (!serverInputExecutor.isShutdown())
                {
                    serverInputExecutor.shutdownNow();
                }
                System.exit(0);
            }
            catch (IOException e)
            {
            }
        }
    }

    // terminates the client on user inactivity
    private static class Terminator implements Runnable
    {
        private final Socket socket;
        private final PrintWriter out;

        public Terminator(Socket s, PrintWriter out)
        {
            this.socket = s;
            this.out    = out;
        }

        @Override
        public void run()
        {
            try
            {
                System.out.println("Closing socket for inactivity.");
                terminateConnection();
            }
            catch (IOException e)
            {
            }
        }

        private void terminateConnection() throws IOException
        {
            socket.close();
            out.println("logout");
        }
    }

    // reads from the server and prints to the client
    private static class ServerHandler implements Runnable
    {
        private final BufferedReader in;

        public ServerHandler(BufferedReader socketInput)
        {
            this.in = socketInput;
        }

        @Override
        public void run()
        {
            String socketInput;

            try
            {
                socketInput = in.readLine();
                while(socketInput != null)
                {
                    System.out.println(socketInput);
                    socketInput = in.readLine();
                }
            }
            catch (IOException e)
            {
            }
            finally
            {
                try
                {
                    in.close();
                    System.exit(0);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
