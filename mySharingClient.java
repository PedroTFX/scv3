import java.net.Socket;
import javax.net.ssl.*;
import java.io.*;
import java.security.Key;
import java.security.KeyStore;

public class mySharingClient {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java mySharingClient <host>:<port> <username> <password>");
            System.exit(1);
        }

        String host = args[0].split(":")[0];
        int port = Integer.parseInt(args[0].split(":")[1]);
        String username = args[1];
        String password = args[2];

        client(host, port, username, password);
    }

    public static void client(String host, int port, String username, String password) {
        try {
            // Load truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream trustStoreStream = new FileInputStream("truststore.jks")) {
                trustStore.load(trustStoreStream, "serverkeystore".toCharArray());
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // Initialize SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory ssf = sslContext.getSocketFactory();
            SSLSocket socket = (SSLSocket) ssf.createSocket(host, port);

            // Specify cipher suites (optional, for stricter security)
            String[] enabledCiphers = {"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA"};
            socket.setEnabledCipherSuites(enabledCiphers);

            System.out.println("Connected to secure server on port " + port);

            // Set up streams
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Authenticate
            output.println(username + " " + password);

            String serverResponse = input.readLine();
            System.out.println("Server says: " + serverResponse);
            if (!serverResponse.equals("OK-AUTHENTICATED") && !serverResponse.equals("OK-NEW-USER")) {
                System.out.println("Authentication failed");
                socket.close();
                return;
            }

            if(serverResponse.equals("OK-NEW-USER")) {
                KeyStoreAndCertificates.generateCertificate(username);

                FileCoordenator fileCoordenator = new FileCoordenator(input, output, socket.getInputStream(), socket.getOutputStream());
                if(!fileCoordenator.send_file(username + ".cer")){
                    System.out.println("Error sending certificate");
                    socket.close();
                    return;
                }

                // receive the server's truststore
                if(new File("truststore.jks").exists()){
                    System.out.println("Truststore already exists");
                }else{
                    System.out.println("Receiving truststore...");
                    if(!fileCoordenator.receive_file(".")){
                        System.out.println("Error receiving truststore");
                        socket.close();
                        return;
                    }
                    System.out.println("Truststore received");
                }

                // receive the server's truststore
                if(new File("truststore.jks").exists()){
                    System.out.println("Truststore already exists");
                }else{
                    System.out.println("Receiving truststore...");
                    if(!fileCoordenator.receive_file(".")){
                        System.out.println("Error receiving truststore");
                        socket.close();
                        return;
                    }
                    System.out.println("Truststore received");
                }
            }

            // TODO: SEND THE CERTIFICATE TO SERVER AND RECEIVE THE SERVER TRUSTSTORE



            // Menu
            if (Menu.menu(input, output, socket.getInputStream(), socket.getOutputStream(), username)) {
                System.out.println("Logged out");
            } else {
                System.out.println("Error logging out");
            }

            socket.close();
        } catch (Exception e) {
            System.out.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}