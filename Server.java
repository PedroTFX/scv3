import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Server {

    private static int port = 12345;

    public static void main(String[] args) {
        System.err.println("Hello,server !");

        // default port
        if(args.length == 1){
            port = Integer.parseInt(args[0]);
        }
        server(port);

    }

    public static void server(int port){

        try {
            // Create a server socket to listen for incoming client connections
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is waiting for clients on port " + port);

            // Accept a client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Set up input and output streams to communicate with the client
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the message from the client and send a response
            String clientMessage = input.readLine();
            System.out.println("Received from client: " + clientMessage);
            output.println("Hello from Server! You said: " + clientMessage);

            // Close the connection
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
