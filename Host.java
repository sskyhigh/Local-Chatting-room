import javax.swing.*;
import java.io.IOException;
// run this only after you ran the main file
public class Host {
    public static void main(String[] args) throws IOException {
        Client client = new Client("127.0.0.1");
        client.startRun();
        client.setVisible(true);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
