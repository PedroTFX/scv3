import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;




public class OperationExecutioner {

    public static PrintWriter output;

    public static void execute(String operation, PrintWriter out) throws IOException {
        
        output = out;
        String operationCommand = operation.split(" ")[0];
        System.out.println("Operation: " + operation);

        switch (operationCommand) {
            case "CREATE":
                create(operation);
                break;
            case "ADD":
                add(operation);
                break;
            case "UP":
                up(operation);
                break;
            case "DW":
                dw(operation);
                break;
            case "RM":
                rm(operation);
                break;
            case "LW":
                lw();
                break;
            case "LS":
                ls(operation);
                break;
            default:
                System.out.println("Invalid operation");
        }
    }

    //  CREATE <ws> # Criar um novo workspace - utilizador é Owner.
    public static void create(String arguments) throws IOException {
        if (arguments == null || arguments.length() == 0) {
            System.out.println("Invalid workspace name");
            return;
        }

        String username = arguments.split(" ")[arguments.split(" ").length - 1];
        String workspaceName = arguments.split(" ")[1];

        String msg = Workspaces.create(username, workspaceName);
        output.println(msg);
    }


    // ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. 
    // A operação ADD só funciona se o utilizador for o Owner do workspace <ws>.
    public static void add(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length != 4) {
            System.out.println("Invalid number of arguments");
            return;
        }

        String username = arguments.split(" ")[arguments.split(" ").length - 1];
        String collaborator = arguments.split(" ")[1];
        String workspaceName = arguments.split(" ")[2];
        

        output.println(Workspaces.addCollaborator(username, collaborator, workspaceName));
    }


    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }
        String username = arguments.split(" ")[arguments.split(" ").length - 1];

        output.println("UP " + username + " " + arguments);
    }


    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        String username = arguments.split(" ")[arguments.split(" ").length - 1];

        output.println("DW " + username + " " + arguments);
    }


    // RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.
    public static void rm(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        String username = arguments.split(" ")[arguments.split(" ").length - 1];

        output.println("RM " + username + " " + arguments);
    }


    // LW # Lista os workspaces associados ao utilizador.
    public static void lw() throws IOException {
        output.println(Workspaces.getAllWorkspaces());
    }


    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String arguments) throws IOException {
        String username = arguments.split(" ")[arguments.split(" ").length - 1];

        output.println(Workspaces.getAllFilesNames(username, arguments));
    }

}
