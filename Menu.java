import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.io.InputStream;
import java.io.OutputStream;

class Menu{

    public static BufferedReader input;
    public static PrintWriter output;
    public static InputStream inStream;
    public static OutputStream outStream;

    public static boolean menu(BufferedReader in, PrintWriter out, InputStream is, OutputStream os, String username) throws IOException{
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
            String commandName = input.split(" ")[0];
            String[] arguments = input.split(" ");
            
            // switch(commandName){
            //     case "CREATE":
            //         create(username, arguments);
            //         break;
            //     case "ADD":
            //         add(username, arguments);
            //         break;
            //     case "UP":
            //         up(username, arguments);
            //         break;
            //     case "DW":
            //         dw(username, arguments);
            //         break;
            //     case "RM":
            //         rm(username, arguments);
            //         break;
            //     case "LW":
            //         lw(username);
            //         break;
            //     case "LS":
            //         ls(username, arguments);
            //         break;
            //     default:
            //         System.out.println(commandName);
            //         System.out.println("Command not found");
            // }

            if(commandName.equals("CREATE") && arguments.length == 2){
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

    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        
        //TODO FIX THE SENDING OF FILES
        // THE SETUP FOR SENDING FILES ONE BY ONE LIKE UP ROOM USER FILE 
        // AND REPEAT FOR ALL FILES MIGHT BE A POSSIBILITY TO EXPLORE DUE TO THE SIMPLICITY

        // check files
        String files_available = "";
        String[] files = files_available.split(" ");
        File[] files_list = new File(".").listFiles();
        for (int i = 1; i < parts.length; i++){
            for (File file : files_list){
                if(parts[i].equals(file.getName())){
                    files_available += parts[i] + " ";
                }
            }
        }
        if(files_available.equals("")){
            System.out.println("No files available");
            return;
        }else{
            for (int i = 1; i < parts.length; i++){
                for (String file : files){
                    if(parts[i].equals(file)){
                        i++;
                        break;
                    }
                }
                System.out.println(parts[i] + ": Nao Existe");
            }
        }


        output.println("UP " + username + " " + files_available);
        
        // check perms
        String perms = input.readLine();
        if(!(perms.equals("HAS_PERMS"))){
            System.out.println(perms);
            return;
        }

        // send files
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (String file : files){
            // send
            if(fileCoordenator.send_file(file) && input.readLine().equals("OK")){
                System.out.println(file + ": OK");
                continue;
            }else{
                System.out.println(file + ": ERR");
            }
        }

        output.println("EOF");
    }

    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("DW" + " " + username + " " + arguments);
        
        String files_available = input.readLine();
        if(files_available.equals("NOPERMS") || files_available.equals("NOWS")){
            System.out.println(files_available);
            return;
        }
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (String file: files_available.split(" ")){
            for(int i = 1; i < parts.length; i++){
                if(parts[i].equals(file)){
                    if(fileCoordenator.receive_file(".")){
                        System.out.println(parts[i] + " #ficheiro transferido");
                    }
                }else{
                    System.out.println("O ficheiro " + parts[i] + " não existe no workspace indicado");
                }

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
        String line = "";
        String workspaces = "{ ";
        while(!(line = input.readLine()).equals("EOF") || line.equals("")){
            workspaces += line + " ; ";
        }
        workspaces = workspaces.substring(0, workspaces.length() - 2);
        workspaces += " }";
        System.out.println(workspaces);
    }

    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        System.out.println("arguments " + arguments);
        if(parts.length != 1 && !parts[0].equals("LS")){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS" + " " + username + " " + arguments);
        String line = "";
        String files = "{ ";
        while(!(line = input.readLine()).equals("EOF")){
            if (line == "NOPERM" || line == "NOWS"){
                System.out.println(line);
                return;
            }
            else{
                files += line + " ; ";
            }
        }
        files = files.substring(0, files.length() - 2);
        files += " }";
        System.out.println(files);
    }

    
}