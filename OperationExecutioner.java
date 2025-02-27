import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;




public class OperationExecutioner {

    public static PrintWriter output;

    public static void execute(String operation, String username, String arguments, PrintWriter out) throws IOException {
        
        output = out;
        
        switch (operation) {
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
                System.out.println("Invalid operation");
        }
    }

    //  CREATE <ws> # Criar um novo workspace - utilizador é Owner.
    public static void create(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length != 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println(Workspaces.create(username, arguments));
    }


    // ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. 
    // A operação ADD só funciona se o utilizador for o Owner do workspace <ws>.
    public static void add(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println(Workspaces.addCollaborator(username, parts[0], parts[1]));
    }


    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("UP " + username + " " + arguments);
    }


    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("DW " + username + " " + arguments);
    }


    // RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.
    public static void rm(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("RM " + username + " " + arguments);
    }


    // LW # Lista os workspaces associados ao utilizador.
    public static void lw() throws IOException {
        output.println(Workspaces.getAllWorkspaces());
    }


    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String username, String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length != 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS " + username + " " + arguments);
    }

}
