import java.security.NoSuchAlgorithmException;

public class Client_1
{

    public static void main(String[] args) throws Exception
    {
        Client client=new Client("127.0.0.1");
        client.startRunning();
    }
}
