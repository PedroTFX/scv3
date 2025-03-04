import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;




public class OperationExecutioner {

    public static PrintWriter output;
    public static BufferedReader input;


    public static void execute(String operation, PrintWriter out, BufferedReader in) throws IOException {
        
        output = out;
        input = in;
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
        FileCoordenator fileCoordenator = new FileCoordenator(input, output);
        String[] parts = arguments.split(" ");
        if (parts.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }

        // arguments: UP admin new_room users.txt
        String user = parts[1];
        String workspace = parts[2];

        String file_path = Workspaces.findWorkspace(workspace);
        if (file_path.equals("")) {
            output.println("NOWS");
            return;
        }
        if (!file_path.contains(user)) {
            output.println("NOPERMS");
            return;
        }


        System.out.println("Number of files: " + (parts.length - 3));

        for (int i = 3; i < parts.length; i++) {
            fileCoordenator.receive_file(file_path);
        }

        // output.println("UP " + user + " " + arguments);
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
        String workspace = arguments.split(" ")[1];
        output.println(Workspaces.getAllFilesNames(username, workspace));
    }

}
