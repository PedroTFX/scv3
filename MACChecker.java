import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
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
    public static boolean checkMAC(String pathToFile, String password) throws Exception{
        String fileName = Paths.get(pathToFile).getFileName().toString();
        System.out.println("fileName: " + fileName);
        String newMAC = generateMAC(pathToFile, generateAESKeyFromPassword(password, password.getBytes()).toString());

        // read the MAC file
        // System.out.println("pathToFile: " + pathToFile);
        // System.out.println("fileName: " + fileName);
        // System.out.println("macs/" + pathToFile.substring(0, pathToFile.length() - fileName.length() - 1));
        File directory = new File("macs/" + pathToFile.substring(0, pathToFile.length() - fileName.length() - 1));
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        for(File file : files_in_dir) {
            // System.out.println("file: " + file.getName());
            if (!file.isDirectory() && file.getName().split("\\.")[0].equals(fileName.split("\\.")[0])) {
                matchingFile = file;
                break; // Exit the loop once we find a matching file
            }
        }

        // check if the file is null
        if (matchingFile == null) {
            System.out.println("No matching MAC files found with:" + fileName);
            return false;
        }

        // read the MAC file
        byte[] macBytes = Files.readAllBytes(matchingFile.toPath());
        String storedMAC = new String(macBytes);

        // compare the MAC values
        if (!storedMAC.equals(newMAC)) {
            System.out.println("MAC values of <" + matchingFile.getName() + "> do not match.");
            return false;
        }
        return true;
    }

    public static String generateMAC(String filePath, String secretKey) throws Exception {
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        // System.out.println("filePath: " + filePath);
        // System.out.println(Paths.get(filePath).getFileName().toString());

        // test with print
        // System.out.println("filePath: " + filePath);
        // System.out.println("fileContent: " + new String(fileContent));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        byte[] macBytes = mac.doFinal(fileContent);
        return Base64.getEncoder().encodeToString(macBytes); // Convert to Base64 for storage
    }

    public static boolean updateMAC(String filePath, String password) throws Exception {
        // check if the file exists
        if (!new File(filePath).exists()) {
            System.out.println("File does not exist: <" + filePath + ">, .mac to be deleted.");
            return removeMAC(filePath, password);
        }

        // Generate new MAC for the file
        String newMAC = generateMAC(filePath, generateAESKeyFromPassword(password, password.getBytes()).toString());
    
        // Extract file and workspace information
        String file = filePath.split("/")[filePath.split("/").length - 1];
        String workspace = filePath.substring(0, filePath.length() - file.length() - 1);
        // System.out.println("workspace: " + workspace);
        // System.out.println("file: " + file);
    
        // Create the MAC file
        File macFile = new File("macs/" + workspace + "/" + file.split("\\.")[0] + ".mac");
    
        // Ensure the parent directories exist
        File parentDir = macFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                System.out.println("Directories created: " + parentDir.getAbsolutePath());
            } else {
                System.out.println("Failed to create directories: " + parentDir.getAbsolutePath());
                return false;
            }
        }
    
        // Create the MAC file if it doesn't exist
        if (!macFile.exists()) {
            if (macFile.createNewFile()) {
                System.out.println("MAC file created: " + macFile.getAbsolutePath());
            } else {
                System.out.println("Failed to create MAC file: " + macFile.getAbsolutePath());
                return false;
            }
        }
    
        // Write the new MAC to the file
        Files.write(macFile.toPath(), newMAC.getBytes());
        return true;
    }

    private static boolean removeMAC(String filePath, String password) {
        // Extract file and workspace information
        String file = filePath.split("/")[filePath.split("/").length - 1];
        String workspace = filePath.substring(0, filePath.length() - file.length() - 1);
        // System.out.println("workspace: " + workspace);
        // System.out.println("file: " + file);

        // Create the MAC file
        File macFile = new File("macs/" + workspace + "/" + file.split("\\.")[0] + ".mac");
        return macFile.exists() && macFile.delete();
    }

    public static File getMACFile(String pathToFile) {
        // Get the MAC file for the given pathToFile
        File directory = new File("macs/");
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        for(File file : files_in_dir) {
            if (!file.isDirectory() && file.getName().split("\\.")[0].equals(pathToFile.split("\\.")[0])) {
                matchingFile = file;
                break; // Exit the loop once we find a matching file
            }
        }
        return matchingFile;
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


	public static boolean allCheckMACs(String password) {
		// check users.txt
        try {
            if (!checkMAC("users.txt", password)) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error checking users.txt MAC: " + e.getMessage());
            return false;
        }
        // check all files in workspaces
        File directory = new File("workspaces/");
        File[] files_in_dir = directory.listFiles();
        if (files_in_dir == null) {
            System.out.println("No workspaces found.");
            return false;
        }
        for (File file : files_in_dir) {
            if (file.isDirectory()) {
                File[] files_in_workspace = file.listFiles();
                if (files_in_workspace != null) {
                    for (File workspaceFile : files_in_workspace) {
                        try {
                            if (!checkMAC(workspaceFile.getAbsolutePath(), password)) {
                                return false;
                            }
                        } catch (Exception e) {
                            System.out.println("Error checking " + workspaceFile.getName() + " MAC: " + e.getMessage());
                            return false;
                        }
                    }
                }
            }
        }
        return true;
	}


    public static void main(String[] args) throws Exception {
        // System.out.println(updateMAC("users.txt", "users.txt"));    //true
        // System.out.println(updateMAC("test.txt", "users.txt"));     //true
        // System.out.println(checkMAC("test.txt", "users.txt"));      //true
        // System.out.println(checkMAC("test.txt", "uga"));            //false
        // System.out.println(checkMAC("users.txt", "users.txt"));     //true
        // System.out.println(allCheckMACs("users.txt"));              //false
        // System.out.println(updateMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt"));     //true
        // System.out.println(allCheckMACs("users.txt"));              //true
        
        // System.out.println("checkMac: " + checkMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        System.out.println("updateMac: " + updateMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        // System.out.println("checkMac: " + checkMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        
        // System.out.println(generateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));
        // System.out.println(updateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));
    }   
}
