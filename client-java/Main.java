import Helpers.Constants;
import Helpers.Connection;
import Services.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    // args: [ip address, port number, at most once, network failure rate, freshness interval]
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Connection connection;
        String[] initial_input = args;
        while(true) {
            try {
                connection = new Connection(scanner, initial_input[0], Integer.parseInt(initial_input[1]),
                        Integer.parseInt(initial_input[2]), Double.parseDouble(initial_input[3]),
                        Integer.parseInt(initial_input[4]));
                break;
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("Bad parameters command line args. Please type: ");
                System.out.println("{ip address} {port number} " +
                        "{1 for at most once, 0 for at least once} " +
                        "{network failure rate} {freshness interval in milliseconds}");
                initial_input = scanner.nextLine().split(" ");
                System.out.println("");
            }
        }

        System.out.println("Welcome!");
        System.out.println("Checking if the Server is reachable...");
        boolean isReachable = isReachable(initial_input[0] , Integer.parseInt(initial_input[1]));
        if (isReachable) {
            System.out.println("The IP address " + initial_input[0]  + " and port " + Integer.parseInt(initial_input[1]) + " are reachable.");
        } else {
            System.out.println("The IP address " + initial_input[0]  + " and port " + Integer.parseInt(initial_input[1]) + " are not reachable.");
        }
        while (true) {
            System.out.println(Constants.SERVICE_PROMPT);
            int input = Integer.parseInt(connection.scanner.nextLine());
            if (input == Constants.EXIT_ID) {
                break;
            }
            Service requested_service = Service.generate_service(input, connection);
            if (!(requested_service==null)) {
                requested_service.act();
            }
            System.out.println();
        }
        connection.close();
        System.out.println("Goodbye!");
    }

    public static boolean isReachable(String ipAddress, int port) {
        try (Socket socket = new Socket()) {
            // Attempt to connect to the IP address and port
            socket.connect(new InetSocketAddress(ipAddress, port), 1000); // Timeout set to 1 second
            return true; // If connection is successful, return true
        } catch (IOException e) {
            // Connection failed
            return false;
        }
    }

}
