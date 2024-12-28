package dist_servers;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server2 {
    private final int port;
    private final List<String> connections = new ArrayList<>();

    public Server2(int port) {
        this.port = port;
    }

    public void addConnection(String host, int port) {
        connections.add(host + ":" + port);
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server "+ port +" adresli portta calisiyor.");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Yeni bir baglanti " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Server socketinde hata: " + e.getMessage());
            }
        }).start();
    }

    public void startConnections() {
        for (String connection : connections) {
            String[] parts = connection.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            new Thread(() -> {
                try (Socket socket = new Socket(host, port)) {
                    System.out.println("Baglanti kuruldu " + host + ":" + port);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(" "+this.port+"adresli port baglandi");
                } catch (IOException e) {
                    System.err.println("Connection to " + host + ":" + port + " failed: " + e.getMessage());
                }
            }).start();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Cevap: " + inputLine);
            }
        } catch (IOException e) {
            System.err.println("Haberlesme hatasi: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        Server2 server2 = new Server2(5002);

        server2.addConnection("localhost", 5001);
        server2.addConnection("localhost", 5003);

        server2.startServer();
        server2.startConnections();
        
    }
}
