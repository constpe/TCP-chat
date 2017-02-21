import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    static Socket serverListener;
    static boolean isClient;

    static void sendMessage(JFrame mainFrame, String message) {
        try {
            PrintStream out = new PrintStream(serverListener.getOutputStream());
            out.print(message + (char)1);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Server connection error", "Connection error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class Receiver implements Runnable {
        Receiver(JFrame mainFrame, JTextArea messagesArea, JTextField IPField, JTextField portField) {
            Thread t = new Thread(this);
            isConnected = true;
            this.ip = IPField.getText();
            this.messagesArea = messagesArea;
            this.mainFrame = mainFrame;
            this.IPField = IPField;
            this.portField = portField;

            if (portField.getText().equals(""))
                port = 0;
            else
                port = Integer.parseInt(portField.getText());

            t.start();
        }

        private boolean isConnected;
        private String ip;
        private int port;
        private JTextArea messagesArea;
        private JTextField IPField, portField;
        private JFrame mainFrame;

        private String readString(DataInputStream in) throws IOException{
            int c;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((c = in.read()) != 1)
                buffer.write(c);
            return new String(buffer.toByteArray(), "utf-8");
        }

        @Override
        public void run() {
            try {
                serverListener = new Socket(ip, port);
                while (isConnected) {
                    DataInputStream in = new DataInputStream(serverListener.getInputStream());
                    isClient = true;
                    IPField.setEditable(false);
                    portField.setEditable(false);
                    String message = readString(in);
                    if (message != null) {
                        if (!message.equals(""))
                            messagesArea.setText(messagesArea.getText() + message + "\n\n");
                    }
                }
            }
            catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(mainFrame, "Incorrect server port value", "Connection error", JOptionPane.ERROR_MESSAGE);
                isConnected = false;
            }
            catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(mainFrame, "Incorrect server IP", "Connection error", JOptionPane.ERROR_MESSAGE);
                isConnected = false;
            }
            catch (SocketException e) {
                JOptionPane.showMessageDialog(mainFrame, "Server connection error", "Connection error", JOptionPane.ERROR_MESSAGE);
                isConnected = false;
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "Server connection error", "Connection error", JOptionPane.ERROR_MESSAGE);
                isConnected = false;
            }
        }
    }

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("Lab 1 - Chat");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(700, 570);
        mainFrame.setResizable(false);
        JPanel panel = new JPanel();
        mainFrame.getContentPane().add(panel);

        JTextArea messagesArea = new JTextArea(null, 22, 60);
        messagesArea.setLineWrap(true);
        messagesArea.setEditable(false);
        JTextArea inputArea = new JTextArea(null, 4, 60);
        inputArea.setLineWrap(true);
        JButton sendButton = new JButton("Send message");
        sendButton.setPreferredSize(new Dimension(120, 25));
        JTextField IPField = new JTextField(16);
        JTextField portField = new JTextField(5);
        JButton connectButton = new JButton("Connect");
        connectButton.setPreferredSize(new Dimension(99, 25));

        panel.add(new JLabel("Chat"));
        panel.add(new JScrollPane(messagesArea));
        panel.add(new JLabel("Enter your message"));
        panel.add(new JScrollPane(inputArea));
        panel.add(connectButton);
        panel.add(new JLabel("Enter server IP"));
        panel.add(IPField);
        panel.add(new JLabel("Enter server port"));
        panel.add(portField);
        panel.add(sendButton);

        isClient = false;

        portField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if ((keyEvent.getKeyChar() > '9' || keyEvent.getKeyChar() < '0') && keyEvent.getKeyChar() != 8)
                    keyEvent.consume();
                if (portField.getText().length() > 4)
                    keyEvent.consume();
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!isClient)
                    JOptionPane.showMessageDialog(mainFrame, "Not connected to the server", "Connection error", JOptionPane.ERROR_MESSAGE);
                else if (inputArea.getText() != ""){
                    sendMessage(mainFrame, inputArea.getText());
                    inputArea.setText("");
                }
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (isClient)
                    JOptionPane.showMessageDialog(mainFrame, "Already connected", "Connection installed", JOptionPane.INFORMATION_MESSAGE);
                else
                    new Receiver(mainFrame, messagesArea, IPField, portField);
            }
        });

        mainFrame.setVisible(true);
    }
}
