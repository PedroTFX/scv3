import java.io.*;
import java.net.*;

public class mySharingServer {
    private static final int PORT = 12345;
    private static String mac_password;

    public static void main(String[] args) throws Exception {
        int port;

        if (args.length < 1) {
            System.out.println("Usage: java mySharingServer <port> <mac_password>");
            return;
        }
        try{
            port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
            mac_password = args[1];
            
        }catch(NumberFormatException e){
            port = PORT;
            mac_password = args[0];
        }
        if(!MACChecker.allCheckMACs(mac_password)){
            return;
        }
        server(port);
    }

    public static void server(int port){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                System.out.println("Waiting for a new client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, mac_password);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String mac_password;

    public ClientHandler(Socket socket, String mac_password) {
        this.clientSocket = socket;
        this.mac_password = mac_password;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStream inStream = clientSocket.getInputStream();
            OutputStream outStream = clientSocket.getOutputStream()) {

            String user_password = in.readLine();
            String[] user_pass = user_password.split(" ");

            String authentication_result = Authentication.auth(user_pass[0], user_pass[1]);

            // create user's first workspace
            if(authentication_result.equals("OK-NEW-USER")){
                int numberOfWorkspace = 0;
                String workspace = "workspace";
                while(Workspaces.findWorkspace(workspace + numberOfWorkspace) != ""){
                    numberOfWorkspace++;
                }
                Workspaces.create(user_pass[0], workspace + numberOfWorkspace);
            }

            out.println(authentication_result);

            // TODO: how is there no bug for wrong password??

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message received: " + message);
                OperationExecutioner.execute(message, out, in, inStream, outStream, mac_password);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
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

