import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        // System.out.println("checkMAC");
        String fileName = Paths.get(pathToFile).getFileName().toString();
        String newMAC = generateMAC(pathToFile, generateAESKeyFromPassword(password, password.getBytes()).toString());

        // read the MAC file
        
        
        
        String workspace = "";
        if(pathToFile.contains("workspaces")){
            workspace = "workspaces/" + Workspaces.findWorkspace(pathToFile.split("/")[1].split(":")[0]);
        }
        
        File directory = new File("macs/" + workspace);
        File[] files_in_dir = directory.listFiles();
        File matchingFile = null;
        if(files_in_dir == null) {
            System.out.println("No MAC files found in directory: " + directory.getAbsolutePath());
            return false;
        }
        for(File file : files_in_dir) {
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

    // assumes there are no workspaces with the same name
    public static boolean createMacWorkspace(String Owner, String workspaceName){
        File mac_file = new File("macs/workspaces/" + workspaceName + ":" + Owner + ">" + Owner);
        return mac_file.mkdirs();
    }

    // assumes that full permissions and workspace exists
    public static boolean addCollaboratorToMacWorkspace(String user, String collaborator, String workspace){
        String fileName = Workspaces.findWorkspace(workspace);

        // avoid duplicates
        for(String s : fileName.split(">")[1].split(",")) {
            if (s.equals(collaborator)) {
                return true;
            }
        }

        File mac_file = new File("macs/workspaces/" + fileName);
        File mac_renamed = new File("macs/workspaces/" + fileName + "," + collaborator);
        return mac_file.renameTo(mac_renamed);
    }


    public static String generateMAC(String filePath, String secretKey) throws Exception {
        // generate key with SHA256
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        mac.init(keySpec);

        //encode file content
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
        System.out.println("Writing new MAC to file: " + macFile.getAbsolutePath());
        System.out.println("newMAC: " + newMAC);
        Files.write(macFile.toPath(), newMAC.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        return true;
    }

    private static boolean removeMAC(String filePath, String password) {
        // Extract file and workspace information
        String file = filePath.split("/")[filePath.split("/").length - 1];
        String workspace = filePath.substring(0, filePath.length() - file.length() - 1);


        // Create the MAC file
        File macFile = new File("macs/" + workspace + "/" + file.split("\\.")[0] + ".mac");
        return macFile.exists() ? macFile.delete() : true;
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


	public static boolean allCheckMACs(String password) throws Exception {
		// check users.txt
        if (!checkMAC("users.txt", password)) {
            return false;
        }
        
        // check all files in workspaces
        File workspaces = new File("workspaces/");
        File macs = new File("macs/workspaces/");

        File[] workspaces_dir = workspaces.listFiles();
        File[] macs_dir = macs.listFiles();

        if(workspaces_dir.length != macs_dir.length){
            System.err.println("Error: DifNumberOfMacsAndDir");
            return false;
        }

        if(workspaces_dir.length == 0){
            System.out.println("Pass, server empty");
            return true;
        }

        for(int i = 0; i < workspaces_dir.length; i++){
            File[] workspace = new File(workspaces_dir[i].getAbsolutePath()).listFiles();
            File[] mac = new File(macs_dir[i].getAbsolutePath()).listFiles();
            if(workspace.length != mac.length){
                return false;
            }

            for(int file = 0; file < workspace.length; file++){
                System.out.println("workspaces/" + workspaces_dir[i].getName() + "/" + workspace[file].getName());
                if(!checkMAC("workspaces/" + workspaces_dir[i].getName() + "/" + workspace[file].getName(), password)){
                    return false;
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
        // System.out.println(updateMAC("workspaces/UGA:admin>admin,user1/test.txt", "users.txt"));     //true
        // System.out.println(updateMAC("workspaces/UGA:admin>admin,user1/users.txt", "users.txt"));
        System.out.println(allCheckMACs("users.txt"));              //true
        
        System.out.println(updateMAC("workspaces/room:admin>admin/users.txt", "users.txt"));
        System.out.println(updateMAC("workspaces/room:admin>admin/test.txt", "users.txt"));
        System.out.println(updateMAC("workspaces/room:admin>admin/runner.sh", "users.txt"));

        // System.out.println(allCheckMACs("users.txt"));
        // System.out.println(checkMAC("users.txt", "users.txt"));
        // System.out.println("checkMac: " + checkMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        // System.out.println("updateMac: " + updateMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        // System.out.println("checkMac: " + checkMAC("workspaces/UGA:admin>admin,user1/uga.txt", "users.txt")); // true
        // System.out.println("checkAllMacs: " + allCheckMACs("users.txt")); // true
        // System.out.println(generateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));
        // System.out.println(updateMAC("workspaces/" + Workspaces.findWorkspace("UGA") + "/uga.txt", "users.txt"));

        // String password = "users.txt";

        // // TESTS
        // // no workspaces just users.txt
        // System.out.println(allCheckMACs(password));                             // true

        // // empty workspace
        // // add new workspace
        // File file = new File("workspaces/test");
        // System.out.println(file.mkdir());                                      //true
        // // file.mkdir();
        // System.out.println(allCheckMACs(password));                            // true
        // System.out.println(updateMAC("workspaces/test", password));           // true
        // System.out.println(allCheckMACs(password));                            // true

        // // add new file to workspace
        // File file2 = new File("workspaces/test/test.txt");
        // file2.createNewFile();
        // System.out.println(allCheckMACs(password));                            // false
        // System.out.println(updateMAC("workspaces/test/test.txt", password));
        // System.out.println(allCheckMACs(password));                            // true

        // // remove file from workspace
        // file2.delete();
        // System.out.println(allCheckMACs(password));                            // false
        // System.out.println(updateMAC("workspaces/test/test.txt", password));
        // System.out.println(allCheckMACs(password));                            // true



    }   
}
