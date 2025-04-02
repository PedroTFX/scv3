import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;


class Authentication {

    private String username;

    private static String getSaltString() {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int i = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(i));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public static String hash(String toHash){
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Perform the hashing
            byte[] hashBytes = digest.digest(toHash.getBytes());
            
            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Hashing algorithm not found: " + e.getMessage());
            return null;
        }
    }

    public static String auth(String username, String password) {
        if(!existsUser(username)){
            if(registerUserInFile(username, password, getSaltString())){
                return "OK-NEW-USER";
            }
        }else{
            if(authenticateUser(username, password)){
                return "OK-AUTHENTICATED";
            }else{
                return "WRONG-PWD";
            }
        }
        return "ERROR";
    }
    
    public static boolean authenticateUser(String username, String password) {
        try {
            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] part = line.split(":");
                if (part[0].equals(username) && part[1].equals(hash(password + part[2]))) {
                    return true;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return false;
    }

    public static boolean existsUser(String username) {
        try {
            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] part = line.split(":");
                if (part[0].equals(username)) {
                    return true;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return false;
    }

    public static boolean registerUserInFile(String username, String password, String salt) {
        if (existsUser(username)) {
            return false;
        }
        try {
            FileWriter fileWriter = new FileWriter("users.txt", true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(username + ":" + hash(password + salt) + ":" + salt);
            printWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return false;
    }

    // remove user from users file
    public static boolean removeUser(String username){
        try {
            File file = new File("users.txt");
            File tempFile = new File("temp.txt");
            Scanner scanner = new Scanner(file);
            PrintWriter printWriter = new PrintWriter(tempFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] part = line.split(":");
                if (!part[0].equals(username)) {
                    printWriter.println(line);
                }
            }
            scanner.close();
            printWriter.close();
            file.delete();
            tempFile.renameTo(file);
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return false;



    }


    public static void main(String[] args) {
        System.out.println(auth("user1", "user1"));
        System.out.println(auth("user1", "user1"));
        System.out.println(auth("user1", "user2"));
        System.out.println(auth("user2", "?*`:ªÇ_"));
        System.out.println(auth("user2", "?*`:ªÇ_"));
    }

}