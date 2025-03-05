import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            String command = new Scanner(System.in).nextLine();
            String commandName = command.split(" ")[0];
            String arguments = command.substring(command.indexOf(" ") + 1);
            switch(commandName){
                case "CREATE":
                    create(username, arguments);
                    break;
                case "ADD":
                    add(username, arguments);
                    break;
                case "UP":
                    up(username, arguments);
                    break;
                case "DW":
                    dw(username, arguments);
                    break;
                case "RM":
                    rm(username, arguments);
                    break;
                case "LW":
                    lw();
                    break;
                case "LS":
                    ls(username, arguments);
                    break;
                default:
                    System.out.println(commandName);
                    System.out.println("Command not found");
            }
        }
    }

    //  CREATE <ws> # Criar um novo workspace - utilizador é Owner.
    public static void create(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("CREATE" + " " + arguments + " " + username);
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
        output.println("ADD" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }

        output.println("UP " + username + " " + arguments);
        System.out.println("arguments :" + arguments);
        
        // System.out.println("Number of files: " + (parts.length - 1));
        
        
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);

        for(int i = 1; i < parts.length; i++){
            fileCoordenator.send_file(parts[i]);
        }


    }

    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("DW" + " " + username + " " + arguments);
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for(int i = 1; i < parts.length; i++){
            fileCoordenator.receive_file(".");
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
    public static void lw()  throws IOException{
        output.println("LW");
        String line = "";
        while(!(line = input.readLine()).equals("EOF")){
            System.out.println(line);
        }
    }

    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS" + " " + arguments + " " + username);
        String line = "";
        while(!(line = input.readLine()).equals("EOF")){
            System.out.println(line);
        }
    }

    
}