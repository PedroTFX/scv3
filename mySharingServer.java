import java.io.*;
import java.net.*;

public class mySharingServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
        server(port);
    }

    public static void server(int port){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                System.out.println("Waiting for a new client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             InputStream inStream = clientSocket.getInputStream();
             OutputStream outStream = clientSocket.getOutputStream()) {

            String user_password = in.readLine();
            String[] user_pass = user_password.split(" ");
            out.println(Authentication.auth(user_pass[0], user_pass[1]));

            String message;
            // System.out.println("waiting for message");
            // System.out.println(in.readLine());
            while ((message = in.readLine()) != null) {
                System.out.println("Message received: " + message);
                OperationExecutioner.execute(message, out, in, inStream, outStream);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Client disconnected");
        }
    }
}

