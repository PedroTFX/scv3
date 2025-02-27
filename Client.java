import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client {

    public String activeUser = null;

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: java Client <host>:<port> <username> <password>");
            System.exit(1);
        }

        // by default, tests will use localhost as the host
        // and port 12345 to connect to the server
        String host = args[0].split(":")[0];
        int port = Integer.parseInt(args[0].split(":")[1]);
        String username = args[1];
        String password = args[2];
        
        client(host, port, username, password);
    }

    public static void client(String host, int port,  String username, String password) {

        try {
            // Connect to the server
            Socket socket = new Socket(host, port);
            System.out.println("Connected to server on port " + port);

            // Set up input and output streams to communicate with the server
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send a user and pass to the server
            output.println(username + " " + password);

            // Read the server's authentication response
            String server_response = input.readLine();
            System.out.println("Server says: " + server_response);
            if(!server_response.equals("OK-AUTHENTICATED") && !server_response.equals("OK-NEW-USER")){
                System.out.println("Authentication failed");
                socket.close();
                return;
            }

            
            
            // Menu loop
            if(Menu.menu(input, output, username)){
                System.out.println("Logged out");
            } else {
                System.out.println("Error logging out");
            }

            // Close the connection
            socket.close();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
