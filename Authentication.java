import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


class Authentication {

    private String username;

    public static String auth(String username, String password) {
        if(!existsUser(username)){
            if(registerUserInFile(username, password)){
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
                String[] part = line.split(" <///> ");
                if (part[0].equals(username) && part[1].equals(password)) {
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
                String[] part = line.split(" <///> ");
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

    public static boolean registerUserInFile(String username, String password) {
        if (existsUser(username)) {
            return false;
        }
        try {
            FileWriter fileWriter = new FileWriter("users.txt", true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(username + " <///> " + password);
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
                String[] part = line.split(" <///> ");
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


}