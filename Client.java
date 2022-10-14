import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {
    private final JTextField userText;
    private final JTextArea chatWindow;
    private ObjectInput inputStream;
    private ObjectOutputStream outputStream;
    private String _message = "";
    private final String serverIP;
    private Socket socket;
    private final int port = 12001;
    private JButton button;

    // constructor
    public Client(String host) {
        super("Client");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            sendButton(e.getActionCommand());
                            userText.setText(null);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(400, 200);
        setVisible(true);
        button = new JButton("Send");
        button.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            sendData(e.getActionCommand());
                            userText.setText("");
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
        );
        add(button, BorderLayout.EAST);
    }

    public void startRun() throws IOException {
        try {
            connectServer();
            StreamSetup();
            whileChatting();
        } catch (EOFException exception) {
            showMessage("\n Client terminated connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    // connecting to the server.
    public void connectServer() throws IOException {
        showMessage("Attempting to connect..");
        socket = new Socket(InetAddress.getByName(serverIP), port);
        showMessage("Connected to: " + socket.getInetAddress().getHostName());
    }

    // set up the streams to set up message.
    public void StreamSetup() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("\n Connection has been established. ");
    }

    public void whileChatting() throws IOException {
        _permissionToType(true);
        do {
            try {
                _message = (String) inputStream.readObject();
                showMessage("\n " + _message);
            } catch (ClassNotFoundException exception) {
                showMessage("\n not readable");
            }
        } while (!_message.equals("Client - End"));
    }

    // closes the connection
    private void closeConnection() throws IOException {
        showMessage("Closing the chat");
        _permissionToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends msg to server
    private void sendData(String _message) throws IOException {
        try {
            outputStream.writeObject("Client - " + _message);
            outputStream.flush();
            showMessage("\n Client: " + _message);
        } catch (IOException ioException) {
            chatWindow.append("\n error occurred " + ioException);
        }
    }

    private void sendButton(String actionCommand) throws IOException {
        try {
            actionCommand = userText.getText();
            outputStream.writeObject("Client - " + actionCommand);
            outputStream.flush();
            showMessage("\n Client: " + actionCommand);

        } catch (IOException ioException) {
            chatWindow.append("\n error occurred " + ioException);
        }
    }

    private void showMessage(final String s) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(s);
                    }
                }
        );
    }

    private void _permissionToType(boolean check) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userText.setEditable(check);
                    }
                }
        );
    }
}
