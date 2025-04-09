import java.io.*;
import java.net.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class mySharingServer {
    private static String mac_password;

    public static void main(String[] args) throws Exception {
        int port;

        if (args.length < 1) {
            System.out.println("Usage: java mySharingServer <port> <mac_password>");
            return;
        }
        try {
            port = args.length > 0 ? Integer.parseInt(args[0]) : 12345;
            mac_password = args[1];
        } catch (NumberFormatException e) {
            port = 12345;
            mac_password = args[0];
        }

        if (!MACChecker.allCheckMACs(mac_password)) {
            return;
        }

        server(port);
    }

    public static void server(int port) {
        try {
            // Load server keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("server.keystore"), "serverkeystore".toCharArray());

            // KeyManager with server's private key
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "serverkeystore".toCharArray());

            // Load truststore (to trust clients if needed in mutual TLS)
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream("truststore.jks"), "serverkeystore".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            // Set up SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            System.out.println("TLS Server is listening on port " + port);

            while (true) {
                System.out.println("Waiting for a new client...");
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket, mac_password);
                new Thread(clientHandler).start();
            }

        } catch (Exception e) {
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
                MACChecker.updateMAC("./users.txt", mac_password);
                int numberOfWorkspace = 0;
                String workspace = "workspace";
                while(Workspaces.findWorkspace(workspace + numberOfWorkspace) != ""){
                    numberOfWorkspace++;
                }
                System.out.println("user_pass[0]: " + user_pass[0]);
                if(Workspaces.create(user_pass[0], workspace + numberOfWorkspace).equals("OK")){
                    MACChecker.createMacWorkspace(user_pass[0], workspace + numberOfWorkspace);
                }
            }

            out.println(authentication_result);

            if(authentication_result.equals("WRONG-PWD")){
                return;
            }

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

