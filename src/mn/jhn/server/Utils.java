package mn.jhn.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Utils
{
    private Utils()
    {
    }

    // reads the users from the file, transforms them into objects and returns them
    public static Set<User> loadUsersFromFile(String filePath) throws IOException
    {
        Set<User> users = new HashSet<User>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = br.readLine();
        while (line != null)
        {
            String[] userPass = line.split("\\s+");
            User user = new User(userPass[0], userPass[1]);
            users.add(user);
            line = br.readLine();
        }
        br.close();
        return Collections.unmodifiableSet(users);
    }

    // joins strings arrays into a single string. useful for printing
    public static String join(String[] input)
    {
        StringBuilder sb = new StringBuilder();
        for(String value : input)
        {
            sb.append(value);
            sb.append(" ");
        }
        return sb.toString();
    }
}
