package mn.jhn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Set;

public class Runner implements Runnable
{
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private User user;

    public Runner(Socket clientSocket) throws IOException
    {
        this.socket    = clientSocket;
        this.in        = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out       = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run()
    {
        try
        {
            User user;
            String username = "";
            String password;
            int attempts = 3;
            while (attempts > 0)
            {
                this.out.println("Username: ");
                username = this.in.readLine();
                this.out.println("Password: ");
                password = this.in.readLine();

                if (!Validator.validateCredentials(username, password))
                {
                    attempts--;
                    this.out.println("Please enter a valid username / password.");
                    this.out.println("You have " + attempts + " attempts remaining.");
                }
                else
                {
                    user = new User(username, password);
                    if (Validator.isLoggedIn(user))
                    {
                        this.out.println("This user is already logged in.");
                        return;
                    }
                    if (Validator.authenticate(user))
                    {
                        this.user = user;
                        break;
                    }
                    else
                    {
                        attempts--;
                        this.out.println("Wrong username / password.");
                        this.out.println("You have " + attempts + " attempts remaining.");
                    }
                }
            }

            if (attempts == 0)
            {
                Server.getBlockedIps().put(this.socket.getInetAddress(), new Date());
                this.out.println("You have been banned for " + Validator.getBlockTime() + " seconds.");
                return;
            }

            registerClient();

            out.println("Logged in.");
            out.println("Welcome!");

            while (true)
            {
                String message = in.readLine();
                if (message == null) { this.out.println("Command not supported."); continue; }
                String firstWord = message.split("\\s+")[0];
                Set<String> commands = Command.getCommands();
                if (!commands.contains(firstWord)) { this.out.println("Command not supported."); continue; }

                for (PrintWriter writer : Server.getCurrentWriters())
                {
                    writer.println(username + ": " + message);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            unregisterClient();
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

    private void registerClient()
    {
        Server.getLoggedInUsers().add(this.user);
        Server.getCurrentWriters().add(this.out);
    }

    private void unregisterClient()
    {
        if (this.user != null)
        {
            Server.getLoggedInUsers().remove(this.user);
        }
        Server.getCurrentWriters().remove(this.out);
    }
}
