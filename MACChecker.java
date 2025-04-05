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
    public static boolean checkMAC(String filename, String password) throws Exception{
        String f = Paths.get(filename).getFileName().toString();
        String newMAC = generateMAC(f, generateAESKeyFromPassword(password, password.getBytes()).toString());

        // read the MAC file
        File directory = new File("macs/");
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        for(File file : files_in_dir) {
            // System.out.println("file: " + file.getName());
            if (!file.isDirectory() && file.getName().split("\\.")[0].equals(f.split("\\.")[0])) {
                matchingFile = file;
                break; // Exit the loop once we find a matching file
            }
        }

        // check if the file is null
        if (matchingFile == null) {
            System.out.println("No matching MAC files found with:" + f);
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

        System.out.println("filePath: " + filePath);
        System.out.println(Paths.get(filePath).getFileName().toString());

        // test with print
        System.out.println("filePath: " + filePath);
        System.out.println("fileContent: " + new String(fileContent));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        byte[] macBytes = mac.doFinal(fileContent);
        return Base64.getEncoder().encodeToString(macBytes); // Convert to Base64 for storage
    }

    public static boolean updateMAC(String filePath, String password) throws Exception {
        // Generate new MAC for the file
        // System.out.println("filepath: " + filePath);
        String newMAC = generateMAC(filePath, generateAESKeyFromPassword(password, password.getBytes()).toString());
        // System.out.println("newMAC: " + newMAC);
        try {
            String file = filePath.split("/")[filePath.split("/").length - 1];
            String workspace = filePath.substring(0, filePath.length() - file.length() - 1);
            System.out.println("workspace: " + workspace);
            System.out.println("file: " + file);

            File macFile = new File("macs/" + workspace + "/" + file.split("\\.")[0] + ".mac");
            Files.write(macFile.toPath(), newMAC.getBytes());
        }catch (Exception e) {
            System.out.println("Error updating MAC: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static File getMACFile(String filename) {
        // Get the MAC file for the given filename
        File directory = new File("macs/");
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        for(File file : files_in_dir) {
            if (!file.isDirectory() && file.getName().split("\\.")[0].equals(filename.split("\\.")[0])) {
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

        // System.out.println(generateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));
        System.out.println(updateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));
    }   
}
