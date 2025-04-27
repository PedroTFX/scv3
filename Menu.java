import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.security.PublicKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;

class Menu{

    public static BufferedReader input;
    public static PrintWriter output;
    public static InputStream inStream;
    public static OutputStream outStream;

    public static boolean menu(BufferedReader in, PrintWriter out, InputStream is, OutputStream os, String username) throws Exception{
        input = in;
        output = out;
        inStream = is;
        outStream = os;

        System.out.println("• CREATE <ws> # Criar um novo workspace - utilizador é Owner. ");
        System.out.println("• ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. A operação ADD só funciona se o utilizador for o Owner do workspace <ws>. ");
        System.out.println("• UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.  ");
        System.out.println("• DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local. ");
        System.out.println("• RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.  ");
        System.out.println("• LW # Lista os workspaces associados ao utilizador. ");
        System.out.println("• LS <ws> # Lista os ficheiros dentro de um workspace.");
    


        while(true){
            String input = new Scanner(System.in).nextLine();

            // TODO: FAZER UM ROTINA PARA VERIFICAR SE A TRUSTSTORE ESTA UPDATED COM OS POSSIVEIS NOVOS USERS

            // check if there are special characters
            String commandName = input.split(" ")[0];
            String[] arguments = input.split(" ");
            
            if(commandName.equals("CREATE") && arguments.length == 3){
                String args = input.substring(7);
                if(args.matches(".*[!@#$%^&*()_+=\\[\\]{};':\"\\\\|,.<>/?].*")){
                    System.out.println("Invalid characters");
                    continue;
                }
                create(username, input.substring(7));
            }
            else if(commandName.equals("ADD") && arguments.length == 3){
                add(username, input.substring(4));
            }
            else if(commandName.equals("UP") && arguments.length > 2){
                up(username, input.substring(3));
            }
            else if(commandName.equals("DW") && arguments.length > 2){
                dw(username, input.substring(3));
            }
            else if(commandName.equals("RM") && arguments.length > 2){
                rm(username, input.substring(3));
            }
            else if(commandName.equals("LW")){
                lw(username);
            }
            else if(commandName.equals("LS") && arguments.length == 2){
                ls(username, input.substring(3));
            }
            else{
                System.out.println("Command not found or invalid number of arguments");
            }
        }
    }

    //  CREATE <ws> # Criar um novo workspace - utilizador é Owner.
    public static void create(String username, String arguments) throws IOException{
        output.println("CREATE" + " " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    // ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. 
    // A operação ADD só funciona se o utilizador for o Owner do workspace <ws>.
    public static void add(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("ADD" + " " + username + " " + arguments);
        System.out.println(input.readLine());
    }


//    UPLOAD PROCESS – Step by Step
// 1. Client requests to upload a file to a workspace.
// 2. Server checks if the user has permission to access the workspace.
// If yes:

// The server sends the user’s version of the workspace key, encrypted with their public key.

// This file is named: <workspace>.key.<user-id>

// 3. Client decrypts the workspace key.
// The client uses their private RSA key to decrypt the file and retrieve the AES symmetric key for that workspace.

// 4. Client signs the original file (before encryption).
// Uses their private RSA key to generate a digital signature of the original file.

// The result is saved in: <filename>.signed.<user-id>
// 5. Client encrypts the original file using the workspace AES key.
// The original file is encrypted with AES using the decrypted workspace key.

// 6. Client uploads both files to the server:
// The encrypted file.

// The signature file (.signed.<user-id>).

// The server stores both. The server cannot decrypt the file or verify the signature — that’s intentional.


    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String username, String arguments) throws Exception{
        String[] parts = arguments.split(" ");
        
        // check files
        String files_available = "";
        File[] files_list = new File(".").listFiles();
        for (int i = 1; i < parts.length; i++){
            for (File file : files_list){
                if(parts[i].equals(file.getName())){
                    files_available += parts[i] + " ";
                }
            }
        }

        // System.out.println("files available: " + files_available);

        if(files_available.equals("")){
            System.out.println("No files available");
            return;
        }

        output.println("UP " + username + " " + parts[0] + " " + files_available);
        
        // check perms
        String perms = input.readLine();
        if(!(perms.equals("HAS_PERMS"))){
            System.out.println(perms);
            return;
        }

        // get the workspace key decrypt it
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        String workspace_password;

        if(fileCoordenator.receive_file(".")){
            workspace_password = WorkspacePasswordManager.decriptWorkspacePassword(username, parts[0] + ".key." + username);
            if(workspace_password == null){
                System.out.println("Error decrypting the workspace key");
                return;
            }
        }else{
            System.out.println("Error receiving the workspace key");
            return;
        }

        // TODO: AFTER GETTING PASSWORD DELETE THE FILE


        // send files
        for (String file : files_available.split(" ")){
            // send
            //TODO: GET BETTER EXTENSION
            String signed_file_name = file + ".signed." + username;
            String keyStorePath = username + ".keystore.jks";
            if(WorkspacePasswordManager.encryptFile(workspace_password, file, file + ".enc")){
                if(fileCoordenator.send_file(file + ".enc") && input.readLine().equals("OK")){
                    if(KeyStoreAndCertificates.signFile(file, keyStorePath, username, "mykey", signed_file_name) && fileCoordenator.send_file(signed_file_name) && input.readLine().equals("OK")){
                        System.out.println(file + ": OK");
                        continue;
                    }else{
                        System.out.println(file + ".signed: ERR");
                    }
                }else{
                    System.out.println(file + ".enc" + ": ERR");
                }
            }
        }

        // output.println("EOF");
    }

    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String username, String arguments) throws Exception{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }

        output.println("DW" + " " + username + " " + arguments);

        // check perms
        String files_available = input.readLine();
        if(files_available.equals("NOPERMS") || files_available.equals("NOWS")){
            System.out.println(files_available); // NOPERMS or NOWS
            return;
        }

        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        // receive workspace key
        if(!fileCoordenator.receive_file(".")){
            System.out.println("Error receiving the workspace key");
            return;
        }

        // System.out.println(files_available);
        if(files_available.split(" ").length == 0){
            System.out.println("No files available");
            return;
        }

        ArrayList<String> list_files_available = new ArrayList<String>(Arrays.asList(files_available.split(" ")));        
        String workspace_password = WorkspacePasswordManager.decriptWorkspacePassword(username, parts[0] + ".key." + username);
        if(workspace_password == null){
            System.out.println("Error decrypting the workspace key");
            return;
        }

        
        for (String file: list_files_available) {
            if(fileCoordenator.receive_file(".")){
                output.println("OK");
                if(file.contains(".signed.")){
                    continue;
                }
                WorkspacePasswordManager.decryptFile(workspace_password, file, file.substring(0, file.length() - 4));
                // System.out.println("File: " + file + " received");
            }else{
                System.out.println("ERR");
            }
        }

        // check the signed files
        for (String file : list_files_available){
            if(file.contains(".signed.")){
                continue;
            }

            String decrypted_file = file.substring(0, file.length() - 4);
            String signed_file = decrypted_file + ".signed.";

            // find signed file in directory
            File[] files_list = new File(".").listFiles();
            for (File file1 : files_list){
                if(file1.getName().contains(signed_file)){
                    signed_file = file1.getName();
                    break;
                }
            }

            // System.out.println(signed_file);
            PublicKey publicKey = KeyStoreAndCertificates.getPublicKeyFromTruststore("truststore.jks", "serverkeystore", signed_file.split("\\.signed\\.")[1]);
            // System.out.println(signed_file.split("\\.signed\\.")[1]);
            if (publicKey == null || !KeyStoreAndCertificates.verifyFileWithPublicKey(decrypted_file, publicKey, signed_file)) {
                System.err.println("File: " + decrypted_file + " was not verified");
                continue;
            }
            System.out.println("File: " + decrypted_file + " - OK");

            // delete the signed file
            // delete the encrypted file
            try {
                File enc_file = new File(decrypted_file + ".enc");
                enc_file.delete();

                File signed_file1 = new File(signed_file);
                signed_file1.delete();
            } catch (Exception e) {
                System.err.println("Error deleting files: " + e.getMessage());
            }
        }

    }

    // RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.
    public static void rm(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }

        output.println("RM" + " " + username + " " + arguments);
        for(int i = 1; i < parts.length; i++){
            System.out.println(input.readLine());
        }
    }

    // LW # Lista os workspaces associados ao utilizador.
    public static void lw(String username)  throws IOException{
        output.println("LW " + username);
        System.out.println(input.readLine());
    }

    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 1 && !parts[0].equals("LS")){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS" + " " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    
}