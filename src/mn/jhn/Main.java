package mn.jhn;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        new Server(4040).start();
    }
}
