package Clients;

import java.io.*;
import java.net.*;

public class Client3 {
    private final String serverHost;
    private final int serverPort;

    public Client3(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void start() {
        try (Socket socket = new Socket(serverHost, serverPort)) {
            System.out.println("Server'a baglandi: " + serverHost + ":" + serverPort);

            // Sunucuya mesaj gönderme
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Merhaba Server3, bu Client3'dan bir mesaj!");

            // Sunucudan gelen mesajları dinleme
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println("Sunucudan gelen mesaj: " + response);
            }
        } catch (IOException e) {
            System.err.println("Sunucuya baglanirken hata olustu: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Server3'nin varsayılan portu 5003
        Client3 client3 = new Client3("localhost", 5003);
        client3.start();
    }
}
