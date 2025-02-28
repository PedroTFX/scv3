import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

class Menu{

    public static BufferedReader input;
    public static PrintWriter output;

    public static boolean menu(BufferedReader in, PrintWriter out, String username) throws IOException{
        input = in;
        output = out;

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

    public static void create(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("CREATE" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    public static void add(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("ADD" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    public static void up(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("UP" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    public static void dw(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("DW" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    public static void rm(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("RM" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    public static void lw()  throws IOException{
        output.println("LW");
        System.out.println(input.readLine());
    }

    public static void ls(String username, String arguments) throws IOException{
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS" + " " + arguments + " " + username);
        System.out.println(input.readLine());
    }

    
}