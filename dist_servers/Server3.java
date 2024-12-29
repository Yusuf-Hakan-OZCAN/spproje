package dist_servers;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server3 {
    private final int port;
    private final List<String> connections = new ArrayList<>();

    public Server3(int port) {
        this.port = port;
    }

    public void addConnection(String host, int port) {
        connections.add(host + ":" + port);
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server " + port + " adresli portta calisiyor.");
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
            int connectionPort = Integer.parseInt(parts[1]); // Değişken adı güncellendi

            new Thread(() -> {
                try (Socket socket = new Socket(host, connectionPort)) {
                    System.out.println("Baglanti kuruldu " + host + ":" + connectionPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(" " + this.port + " adresli port baglandi");
                } catch (IOException e) {
                    System.err.println("Connection to " + host + ":" + connectionPort + " failed: " + e.getMessage());
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
        Server3 Server3 = new Server3(5003);

        Server3.addConnection("localhost", 5001);
        Server3.addConnection("localhost", 5002);

        Server3.startServer();
        Server3.startConnections();
    }
}
