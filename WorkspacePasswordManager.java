import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

public class WorkspacePasswordManager {

    // Generate a random salt
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // 16-byte salt
        random.nextBytes(salt);
        return salt;
    }

    // generate workspace password
    public static SecretKey generateWorkspacePassword(String userPassword, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(userPassword.toCharArray(), salt, 65536, 128);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
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


    public static boolean encryptFile(String password, String inputFile, String outputFile) throws Exception {
        byte[] salt = generateSalt();
        byte[] iv = generateSalt();

        File input_file = new File(inputFile);
        File output_file = new File(outputFile);

        SecretKey secretKey = generateWorkspacePassword(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        try (FileOutputStream fos = new FileOutputStream(output_file);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher);
             FileInputStream fis = new FileInputStream(input_file)) {

            // Save salt + iv at start of file
            fos.write(salt);
            fos.write(iv);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, read);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean decryptFile(String password, String encryptedFile, String outputFile) throws Exception {
        
        File encrypted_file = new File(encryptedFile);
        File output_file = new File(outputFile);
        
        try (FileInputStream fis = new FileInputStream(encrypted_file)) {
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];

            // Read salt and IV from the file
            fis.read(salt);
            fis.read(iv);

            SecretKey secretKey = generateWorkspacePassword(password, salt);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(output_file)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    


    public static void main(String[] args) throws Exception {
        byte[] salt_bytes = generateSalt();
        String salt = Base64.getEncoder().encodeToString(salt_bytes);
        // String workspace_password = generateWorkspacePassword("password", salt_bytes);
        System.out.println(salt);
        // System.out.println(workspace_password);

        System.out.println(encriptWorkspacePassword("seisletras", "workspace0.key.seisletras", "password" + ":" + salt));
        System.out.println(decriptWorkspacePassword("seisletras", "workspace0.key.seisletras"));

        encryptFile("password", "test.txt", "test.txt.enc");
        decryptFile("password", "test.txt.enc", "test_dec.txt");
    }
}
