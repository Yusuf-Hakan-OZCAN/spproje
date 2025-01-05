package Clients;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import com.google.protobuf.Timestamp;

// Import generated protobuf classes
import protobuf.SubscriberProto.Subscriber;

public class Client2 {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6002);
             OutputStream output = socket.getOutputStream();
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Enter your name:");
            String name = scanner.nextLine();

            // Create a Subscriber object
            Subscriber subscriber = Subscriber.newBuilder()
                .setNameSurname(name)
                .setStatus("SUBS") // Subscription request
                .setLastAccessed(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build())
                .build();

            // Send the Subscriber object to the server
            subscriber.writeTo(output);

            System.out.println("Subscriber object sent: " + subscriber);

        } catch (Exception e) {
            System.err.println("Error in Client2: " + e.getMessage());
        }
    }
}
