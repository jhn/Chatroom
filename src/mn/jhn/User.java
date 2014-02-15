package mn.jhn;

public class User
{
    private final String username;
    private final String password;

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        User user = (User) o;

        return this.password.equals(user.password) && this.username.equals(user.username);
    }

    @Override
    public int hashCode()
    {
        int result = this.username.hashCode();
        result = 31 * result + this.password.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "username='" + this.username + '\'' +
                ", password='" + this.password + '\'' +
                '}';
    }
}
