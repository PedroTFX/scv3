import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Certificate;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.io.IOException;

public class KeyStoreAndCertificates {


    /**
     * Generates a keystore and self-signed certificate for the given username.
     * @param username
     */
    public static void generateCertificate(String username){
        try {
            String keystoreName = username + ".keystore.jks";
            String certFileName = username + "_cert.cer";
            String alias = "mykey";
            String storepass = username;
            String keypass = username;

            // Step 1: Generate the keystore with self-signed certificate
            ProcessBuilder generateKey = new ProcessBuilder(
                "keytool",
                "-genkeypair",
                "-alias", alias,
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "365",
                "-keystore", keystoreName,
                "-storepass", storepass,
                "-keypass", keypass,
                "-dname", "CN=" + username + ", OU=FC, O=UL, L=Lisbon, ST=LX, C=Portugal"
            );

            runCommand(generateKey, "Generating keystore and self-signed certificate");

            // Step 2 (optional): Export the certificate to a separate file
            ProcessBuilder exportCert = new ProcessBuilder(
                "keytool",
                "-exportcert",
                "-alias", alias,
                "-keystore", keystoreName,
                "-storepass", storepass,
                "-file", certFileName
            );

            runCommand(exportCert, "Exporting certificate to " + certFileName);

            System.out.println("Keystore and certificate generation completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes a command using ProcessBuilder and prints the output.
     * @param pb
     * @param action
     * @throws IOException
     * @throws InterruptedException
     */
    private static void runCommand(ProcessBuilder pb, String action) throws IOException, InterruptedException {
        System.out.println(action + "...");
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = errReader.readLine()) != null) {
            System.err.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println(action + " completed successfully.");
        } else {
            System.err.println(action + " failed with exit code " + exitCode);
        }
    }

    /**
     * Signs a file using the private key from the keystore.
     * @param filePath
     * @param keystorePath
     * @param keystorePassword
     * @param alias
     * @param signatureFilePath
     * @return
     */
    public static boolean signFile(String filePath, String keystorePath, String keystorePassword, String alias, String signatureFilePath) {
        try {
            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keystore.load(fis, keystorePassword.toCharArray());
            }

            // Retrieve the private key
            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray());
            if (privateKey == null) {
                throw new Exception("Private key not found for alias: " + alias);
            }

            // Read the file to be signed
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));

            // Sign the file
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(fileData);
            byte[] digitalSignature = signature.sign();

            // Save the signature to a file
            try (FileOutputStream fos = new FileOutputStream(signatureFilePath)) {
                fos.write(Base64.getEncoder().encode(digitalSignature));
            }

            System.out.println("File signed successfully. Signature saved to: " + signatureFilePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies the signature of a file using the public key from the keystore.
     * @param filePath
     * @param keystorePath
     * @param keystorePassword
     * @param alias
     * @param signatureFilePath
     * @return
     */
    public static boolean verifyFile(String filePath, String keystorePath, String keystorePassword, String alias, String signatureFilePath) {
        try {
            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keystore.load(fis, keystorePassword.toCharArray());
            }
    
            // Retrieve the certificate
            java.security.cert.Certificate certificate = keystore.getCertificate(alias);
            if (certificate == null) {
                throw new Exception("Certificate not found for alias: " + alias);
            }
    
            // Extract the public key from the certificate
            PublicKey publicKey = certificate.getPublicKey();
    
            // Read the file to be verified
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
    
            // Read the signature
            byte[] signatureBytes = Base64.getDecoder().decode(Files.readAllBytes(Paths.get(signatureFilePath)));
    
            // Verify the signature
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(fileData);
            boolean isVerified = signature.verify(signatureBytes);
    
            if (isVerified) {
                System.out.println("Signature verified successfully.");
            } else {
                System.out.println("Signature verification failed.");
            }
    
            return isVerified;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateTrustStore(String certFilePath, String alias, String truststorePath, String truststorePassword) {
        try {
            // Import the required class directly in the function
            java.security.cert.Certificate userCert;
    
            // Load the truststore
            KeyStore truststore = KeyStore.getInstance("JKS");
            try (FileInputStream truststoreStream = new FileInputStream(truststorePath)) {
                truststore.load(truststoreStream, truststorePassword.toCharArray());
            }
    
            // Load the user's certificate
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            try (FileInputStream certStream = new FileInputStream(certFilePath)) {
                userCert = certFactory.generateCertificate(certStream);
    
                // Add the user's certificate to the truststore
                truststore.setCertificateEntry(alias, userCert);
            }
    
            // Save the updated truststore
            try (FileOutputStream truststoreOut = new FileOutputStream(truststorePath)) {
                truststore.store(truststoreOut, truststorePassword.toCharArray());
            }
    
            System.out.println("Certificate for alias '" + alias + "' added to truststore: " + truststorePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateCertificate(args.length > 0 ? args[0] : "defaultUser");

        String keystorePath = "defaultUser.keystore.jks";
        String keystorePassword = "defaultUser";

        System.out.println(signFile("test.txt", keystorePath, keystorePassword, "mykey", "test.sign"));
        System.out.println(verifyFile("test.txt", keystorePath, keystorePassword, "mykey", "test.sign"));
    }
}
