package mn.jhn;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Set<User> users = Utils.loadUsersFromFile("resources/user_pass.txt");
        Server server = new Server(4040);
        server.registerUsers(users);
        server.start();
    }
}
