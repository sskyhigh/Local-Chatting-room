import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BackEnd extends JFrame {
    private final JTextField userTextMsg;
    private final JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket serverSocket;
    private Socket socket;

    //building a constructor
    public BackEnd() {
        super("Messenger");
        userTextMsg = new JTextField();
        // set to false before connection.
        userTextMsg.setEditable(false);
        userTextMsg.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            sendMessage(e.getActionCommand());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        userTextMsg.setText(" ");
                    }
                }
        );
        add(userTextMsg, BorderLayout.NORTH);
        // user can chat here
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(400, 200);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //Setup and configure the server.
    // 100 - queue length , how many people can join
    // 6789 - where is the server.
    public void startRun() throws IOException {
        try {
            // attempt to connect to host' IP
            serverSocket = new ServerSocket(6789, 100);
            while (true) {
                try {
                    // setting up a connection.
                    waitingForConnection();
                    // after connected
                    // sets up the input and output.
                    StreamSetup();
                    _whileChatting();
                } catch (EOFException exception) {
                    showMessage("\n BackEnd has ended connection");
                } finally {
                    endConversation();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // wait for the connection
    // display when connected.
    public void waitingForConnection() throws IOException {
        showMessage("Waiting for someone to connect...");
        socket = serverSocket.accept();
        System.out.println("Connected to " + socket.getInetAddress().getHostName());
    }

    // stream to receive and send data
    public void StreamSetup() throws IOException {
        // pathway to connect to another computer.
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("Connection has been established");
    }

    // chatting
    private void _whileChatting() throws IOException {
        String Message = "Connected to chat";
        sendMessage(Message);
        _permissionToType(true);
        do {
            // conversation
            try {
                Message = (String) inputStream.readObject();
                showMessage("\n " + Message);
            } catch (ClassNotFoundException exception) {
                showMessage("Not readable");
            }
        } while (!Message.equals("CLIENT - END"));
    }

    // Close the sockets after chatting
    // shows that conversation has ended.
    private void endConversation() throws IOException {
        showMessage("\nClosing the connection.. \n");
        _permissionToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // sends msgs to client
    private void sendMessage(String _message) throws IOException {
        try {
            outputStream.writeObject("Sky: " + _message);
            outputStream.flush();
            showMessage("\n Sky" + _message);
        } catch (IOException exception) {
            chatWindow.append("\n Error, unable to send msg");
        }
    }

    // updates the chat window
    private void showMessage(final String text) {
        // this updates chat window + GUI
        // updates and passes on msgs to the chat window.
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }

    // allow user to type inside box
    private void _permissionToType(final boolean check) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userTextMsg.setEditable(check);
                    }
                }
        );
    }
}
