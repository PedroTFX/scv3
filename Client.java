import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {

    public String activeUser = null;

    public static void main(String[] args) {

        System.out.println(args[0]);
        if (args.length != 1) {
            System.err.println("Usage: java Client <host>:<port>");
            System.exit(1);
        }

        // by default, tests will use localhost as the host
        // and port 12345 to connect to the server
        String host = args[0].split(":")[0];
        int port = Integer.parseInt(args[0].split(":")[1]);

        Authentication authentication = new Authentication("admin", "admin");
        

        client(host, port);
    }

    public static void client(String host, int port) {

        try {
            // Connect to the server
            Socket socket = new Socket(host, port);
            System.out.println("Connected to server on port " + port);

            // Set up input and output streams to communicate with the server
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            output.println("Hello from Client!");

            // Read the server's response
            String serverResponse = input.readLine();
            System.out.println("Server says: " + serverResponse);

            // Close the connection
            socket.close();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
