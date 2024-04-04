import Services.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import Driver.Connection;
import Driver.Constants;

public class Main {

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
                System.out.println("Commandline arguments are not correct. Must be of the format   : ");
                System.out.println("{IP address} {Port number} " +
                        "{1 for at-most-once, 0 for at-least-once} " +
                        "{network-failure-rate} {freshness-interval(ms)}");
                initial_input = scanner.nextLine().split(" ");
                System.out.println("");
            }
        }

        System.out.println("Welcome to our DFS Client!");
    
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

}
