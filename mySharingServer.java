import java.io.*;
import java.net.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;

public class mySharingServer {
    private static String mac_password;

    public static void main(String[] args) throws Exception {
        int port;

        if (args.length < 1) {
            System.out.println("SERVER: " + "Usage: java mySharingServer <port> <mac_password>");
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
            System.out.println("SERVER: " + "TLS Server is listening on port " + port);

            while (true) {
                System.out.println("SERVER: " + "Waiting for a new client...");
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("SERVER: " + "New client connected");

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

            out.println(authentication_result);


            // create user's first workspace
            if(authentication_result.equals("OK-NEW-USER")){
                MACChecker.updateMAC("./users.txt", mac_password);
                FileCoordenator fileCoordenator = new FileCoordenator(in, out, inStream, outStream);
                if(!fileCoordenator.receive_file(".")){
                    System.out.println("SERVER: " + "Error receiving certificate");
                    return;
                }
                System.out.println("SERVER: " + "Certificate received");
                
                // pretty self explanatory: update the truststore with the new user's certificate
                KeyStoreAndCertificates.updateTrustStore(user_pass[0] + ".cer", user_pass[0], "truststore.jks", "serverkeystore");
                
                // send truststore to the client
                String truststore_action = in.readLine();
                System.out.println("SERVER: " + "Truststore action: " + truststore_action);
                if(truststore_action.equals("TRUSTSTORE-REQUEST")){ //this shit is necessary cause of lazy development environment
                    System.out.println("SERVER: " + "Sending truststore...");
                    if(!fileCoordenator.send_file("truststore.jks")){
                        System.out.println("SERVER: " + "Error sending truststore");
                        return;
                    }
                    System.out.println("SERVER: " + "Truststore sent");
                }else if(truststore_action.equals("TRUSTSTORE-EXISTS")){
                    System.out.println("SERVER: " + "Truststore already exists");
                }else{
                    System.out.println("SERVER: " + "Error receiving truststore request");
                    return;
                }

                // Creating the workspace of the user on register
                // finding the workspace number
                int numberOfWorkspace = 0;
                String workspace = "workspace";
                while(Workspaces.findWorkspace(workspace + numberOfWorkspace) != ""){
                    numberOfWorkspace++;
                }

                // Creating the workspace of the user on register
                if(Workspaces.create(user_pass[0], workspace + numberOfWorkspace).equals("OK")){
                    // make random password deu to enunciado lack of specification
                    String key_filename = workspace + numberOfWorkspace + ".key." + user_pass[0];
                    String workspace_password = WorkspacePasswordManager.generateRandomPassword();
                    String workspacePath = "workspaces/" + Workspaces.findWorkspace(workspace + numberOfWorkspace);
                    WorkspacePasswordManager.encriptWorkspacePassword(user_pass[0], workspacePath + "/" + key_filename, workspace_password);
                    System.out.println("SERVER: " + "Workspace path: " + workspacePath);             

                    
                    // DUE TO THE WAY UPDATE MAC WORKS IT CORRECTS THE MISSING FOLDER MORE CORRECTLY THAN THE CREATE MACKWORKSPACE
                    MACChecker.updateMAC(workspacePath + "/" + key_filename, "users.txt");

                    // delete the temp key file
                    File file = new File(key_filename);
                    file.delete();
                }
            }


            if(authentication_result.equals("WRONG-PWD")){
                return;
            }

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("SERVER: " + "Message received: " + message);
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
            System.out.println("SERVER: " + "Client disconnected");
        }
    }


}

