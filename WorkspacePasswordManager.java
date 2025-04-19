import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

public class WorkspacePasswordManager {

    // Generate a random salt
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8]; // 8-byte salt
        random.nextBytes(salt);
        return salt;
    }

    // generate workspace password
    public static String generateWorkspacePassword(String userPassword, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(userPassword.toCharArray(), salt, 65536, 128);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }


    public static boolean encriptWorkspacePassword(String username, String filename, String workspacePassword) {
        try {
            // Path to the user's keystore
            String keystorePath = username + ".keystore.jks";
            String keystorePassword = username; // Assuming the keystore password is the username
            String alias = "mykey"; // Alias for the user's key pair

            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            try (FileInputStream keystoreStream = new FileInputStream(keystorePath)) {
                keystore.load(keystoreStream, keystorePassword.toCharArray());
            }

            // Get the public key from the keystore
            PublicKey publicKey = keystore.getCertificate(alias).getPublicKey();

            // Encrypt the workspace password
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedPassword = cipher.doFinal(workspacePassword.getBytes());

            // Encode the encrypted password as Base64
            String encryptedPasswordBase64 = Base64.getEncoder().encodeToString(encryptedPassword);

            // Save the encrypted password to the specified file
            try (FileWriter fileWriter = new FileWriter(filename)) {
                fileWriter.write(encryptedPasswordBase64);
            }
            System.out.println("Workspace password encrypted and saved to: " + filename);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String decriptWorkspacePassword(String username, String filename) {
        try {
            // Path to the user's keystore
            String keystorePath = username + ".keystore.jks";
            String keystorePassword = username; // Assuming the keystore password is the username
            String alias = "mykey"; // Alias for the user's key pair
    
            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            try (FileInputStream keystoreStream = new FileInputStream(keystorePath)) {
                keystore.load(keystoreStream, keystorePassword.toCharArray());
            }
    
            // Get the private key from the keystore
            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray());
    
            // Read the encrypted password from the file
            String encryptedPasswordBase64 = new String(Files.readAllBytes(Paths.get(filename)));
    
            // Decode the Base64-encoded encrypted password
            byte[] encryptedPassword = Base64.getDecoder().decode(encryptedPasswordBase64);
    
            // Decrypt the workspace password
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedPassword = cipher.doFinal(encryptedPassword);
    
            // Return the decrypted password as a string
            return new String(decryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    


    public static void main(String[] args) throws Exception {
        byte[] salt_bytes = generateSalt();
        String salt = Base64.getEncoder().encodeToString(salt_bytes);
        String workspace_password = generateWorkspacePassword("password", salt_bytes);
        System.out.println(salt);
        System.out.println(workspace_password);

        System.out.println(encriptWorkspacePassword("seisletras", "workspace0.key.seisletras", workspace_password));
        System.out.println(decriptWorkspacePassword("seisletras", "workspace0.key.seisletras"));
    }
}
