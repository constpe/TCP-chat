import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    static class ClientInfo {
        public ClientInfo(InetAddress address, Socket clientListener) {
            this.address = address;
            this.clientListener = clientListener;
        }

        InetAddress address;
        Socket clientListener;
    }

    static boolean containAddress(InetAddress address, ArrayList<ClientInfo> clientsList) {
        boolean result = false;

        for (int i = 0; i < clientsList.size(); i++) {
            if (clientsList.get(i).address.equals(address))
                result = true;
        }

        return result;
    }

    static void remove(InetAddress address, ArrayList<ClientInfo> clientsList) {
        int i = 0;

        while (i < clientsList.size()) {
            if (clientsList.get(i).address.equals(address))
                clientsList.remove(clientsList.get(i));
            else
                i++;
        }
    }

    static class User implements Runnable {
        public User(Socket clientListener, ArrayList<ClientInfo> clientsList) {
            if (!containAddress(clientListener.getInetAddress(), clientsList)) {
                clientsList.add(new ClientInfo(clientListener.getInetAddress(), clientListener));
                System.out.println("Connected client with IP " + clientListener.getInetAddress());
            }
            else {
                remove(clientListener.getInetAddress(), clientsList);
                clientsList.add(new ClientInfo(clientListener.getInetAddress(), clientListener));
            }
            this.clientsList = clientsList;
            this.clientListener = clientListener;
            isConnected = true;
            Thread t = new Thread(this);
            t.start();
        }

        ArrayList<ClientInfo> clientsList;
        private Socket clientListener;
        private boolean isConnected;

        private String readString(DataInputStream in) throws IOException{
            int c;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            while ((c = in.read()) != 1)
                buffer.write(c);

            return new String(buffer.toByteArray(), "utf-8");
        }

        @Override
        public void run() {
            DataInputStream in;
            PrintStream out;
            while (isConnected) {
                try {
                    in = new DataInputStream(clientListener.getInputStream());
                    String message = readString(in);

                    System.out.println("Client with IP " + clientListener.getInetAddress() + " send message");

                    int i = 0;

                    while (i < clientsList.size()) {
                        try {
                            out = new PrintStream(clientsList.get(i).clientListener.getOutputStream());
                            out.print(message + (char) 1);
                            out.flush();
                            System.out.println("Client with IP " + clientsList.get(i).clientListener.getInetAddress() + " received message");
                            i++;
                        } catch (IOException e) {
                            System.out.println("Disconnected client with IP " + clientsList.get(i).clientListener.getInetAddress());
                            clientsList.remove(clientsList.get(i));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected client with IP " + clientListener.getInetAddress());
                    isConnected = false;
                }
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<ClientInfo> clientsList = new ArrayList<>();
        ServerSocket server;

        try {
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter server port: ");
            int port = reader.nextInt();
            server = new ServerSocket(port);
            System.out.println("Server is working...");

            while (true) {
                new User(server.accept(), clientsList);
            }
        }
        catch (IOException e) {
            System.out.println("An error has occurred during server work\nServer stopped");
            e.printStackTrace();
        }
        catch (java.util.InputMismatchException e) {
            System.out.println("An error has occurred during server work\nServer stopped");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            System.out.println("An error has occurred during server work\nServer stopped");
            e.printStackTrace();
        }
    }
}


