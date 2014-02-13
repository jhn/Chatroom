package mn.jhn;

import java.io.IOException;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException
    {
        Set<User> users = Utils.loadUsersFromFile("resources/user_pass.txt");
        for (User user : users)
        {
            System.out.println(user.getUsername() + " " + user.getPassword());
        }
    }
}
