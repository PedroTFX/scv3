import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class MACChecker {


    // this function will check the folder MAC_Values
    // compare the values with the values of the files users.txt
    // and all workspaces
    public static boolean checkMAC(String filePath, String password) throws Exception{
        File macFolder = new File("macs/");
        File[] macFiles = macFolder.listFiles();
        if (macFiles == null) {
            System.out.println("No MAC files found.");
            return false;
        }

        String newMAC = generateMAC(filePath, generateAESKeyFromPassword(password, password.getBytes()).toString());

        // read the MAC file
        File directory = new File("macs/");
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        for(File file : files_in_dir) {
            if (!file.isDirectory() && file.getName().split("\\.")[0].equals(filePath.split("\\.")[0])) {
                matchingFile = file;
                break; // Exit the loop once we find a matching file
            }
        }

        // check if the file is null
        if (matchingFile == null) {
            System.out.println("No matching MAC files found.");
            return false;
        }

        // read the MAC file
        byte[] macBytes = Files.readAllBytes(matchingFile.toPath());
        String storedMAC = new String(macBytes);

        // compare the MAC values
        if (!storedMAC.equals(newMAC)) {
            System.out.println("MAC values do not match.");
            return false;
        }
        return true;
    }

    public static String generateMAC(String filePath, String secretKey) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        byte[] macBytes = mac.doFinal(fileContent);
        return Base64.getEncoder().encodeToString(macBytes); // Convert to Base64 for storage
    }

    public static boolean updateMAC(String filePath, String password) throws Exception {
        // Generate new MAC for the file
        System.out.println("filepath: " + filePath);
        String newMAC = generateMAC(filePath, generateAESKeyFromPassword(password, password.getBytes()).toString());
        System.out.println("newMAC: " + newMAC);
        try {
            
            File macFile = new File("macs/" + filePath.split("\\.")[0] + ".mac");
            Files.write(macFile.toPath(), newMAC.getBytes());
        }catch (Exception e) {
            System.out.println("Error updating MAC: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean hasMACChanged(){
        return true;
    }

    // Generate AES-128 key from password
    public static SecretKey generateAESKeyFromPassword(String password, byte[] salt) throws Exception {
        int keyLength = 128; // 128-bit key
        int iterations = 65536; // Number of iterations for PBKDF2

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    public static void main(String[] args) throws Exception {
        // System.out.println(generateMAC("test.txt", "secretKey"));
        // System.out.println(generateMAC("users.txt", "secretKey"));
        // System.out.println(updateMAC("users.txt", "users.txt"));
        // System.out.println(updateMAC("test.txt", "users.txt"));
        System.out.println(checkMAC("test.txt", "users.txt"));
        System.out.println(checkMAC("test.txt", "uga"));
        System.out.println(checkMAC("users.txt", "users.txt"));

    }

	public static boolean allCheckMACs(String password) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'allCheckMACs'");
	}
}
