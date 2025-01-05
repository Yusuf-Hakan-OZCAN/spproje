package dist_servers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import dist_servers.protobuf.ConfigurationProto;
import dist_servers.protobuf.MessageProto;
import dist_servers.protobuf.SubscriberProto;
import dist_servers.protobuf.CapacityProto;
import com.google.protobuf.Timestamp;

public class Server3 {
    private static final int CLIENT_PORT = 6003;
    private static final int ADMIN_PORT = 7003;
    private static final String PLOTTER_HOST = "localhost";
    private static final int PLOTTER_PORT = 8000;
    private static List<SubscriberProto.Subscriber> subscribers = new ArrayList<>();

    public static void main(String[] args) {
        // Start client listener thread
        Thread clientListener = new Thread(() -> listenForClients());
        clientListener.start();

        // Start admin listener thread
        Thread adminListener = new Thread(() -> listenForAdmin());
        adminListener.start();
    }

    private static void listenForClients() {
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {
            System.out.println("Server3 listening for clients on port " + CLIENT_PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream input = clientSocket.getInputStream()) {

                    // Parse the incoming Subscriber object
                    SubscriberProto.Subscriber subscriber = SubscriberProto.Subscriber.parseFrom(input);
                    subscribers.add(subscriber);

                    System.out.println("Received subscriber: " + subscriber);
                } catch (Exception e) {
                    System.err.println("Error processing client: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error in client listener: " + e.getMessage());
        }
    }

    private static void listenForAdmin() {
        try (ServerSocket serverSocket = new ServerSocket(ADMIN_PORT)) {
            System.out.println("Server3 listening for admin on port " + ADMIN_PORT);
    
            while (true) {
                try (Socket adminSocket = serverSocket.accept();
                     InputStream input = adminSocket.getInputStream();
                     OutputStream output = adminSocket.getOutputStream()) {
    
                    // Parse the incoming message (capacity request)
                    CapacityProto.CapacityRequest request = CapacityProto.CapacityRequest.parseFrom(input);
                    System.out.println("Received capacity request for server ID: " + request.getServerId());
    
                    if (request.getServerId() == 3) {
                        // Create and send capacity response
                        CapacityProto.Capacity capacityResponse = CapacityProto.Capacity.newBuilder()
                                .setServerId(3)
                                .setServerStatus(subscribers.size())
                                .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                                .build();
    
                        output.write(capacityResponse.toByteArray());
                        output.flush();
                        System.out.println("Sent capacity response: " + capacityResponse);
                    }
    
                } catch (Exception e) {
                    System.err.println("Error processing admin request: " + e.getMessage());
                }
            }
    
        } catch (Exception e) {
            System.err.println("Error in admin listener: " + e.getMessage());
        }
    }
    

    private static void sendCapacityToPlotter() {
        new Thread(() -> {
            while (true) {
                try {
                    CapacityProto.Capacity capacityResponse = CapacityProto.Capacity.newBuilder()
                            .setServerId(3)
                            .setServerStatus(subscribers.size())
                            .setTimestamp(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                            .build();

                    try (Socket plotterSocket = new Socket(PLOTTER_HOST, PLOTTER_PORT);
                         OutputStream output = plotterSocket.getOutputStream()) {

                        String json = "{\"server_id\": " + capacityResponse.getServerId() + ", \"server_status\": " + capacityResponse.getServerStatus() + ", \"timestamp\": " + capacityResponse.getTimestamp().getSeconds() + "}";

                        output.write(json.getBytes());
                        output.flush();

                        System.out.println("Sent to plotter: " + json);
                    }

                    Thread.sleep(5000); // Wait 5 seconds before next update

                } catch (Exception e) {
                    System.err.println("Error sending to plotter: " + e.getMessage());
                }
            }
        }).start();
    }
}
