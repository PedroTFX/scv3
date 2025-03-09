import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;




public class OperationExecutioner {

    public static PrintWriter output;
    public static BufferedReader input;
    public static InputStream inStream;
    public static OutputStream outStream;


    public static void execute(String operation, PrintWriter out, BufferedReader in, InputStream is, OutputStream os) throws IOException {
        
        output = out;
        input = in;
        inStream = is;
        outStream = os;
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
                lw(operation);
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

        String username = arguments.split(" ")[1];
        String workspaceName = arguments.split(" ")[2];

        output.println(Workspaces.create(username, workspaceName));
    }


    // ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. 
    // A operação ADD só funciona se o utilizador for o Owner do workspace <ws>.
    public static void add(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length != 4) {
            System.out.println("Invalid number of arguments");
            return;
        }

        String username = arguments.split(" ")[1];
        String collaborator = arguments.split(" ")[2];
        String workspaceName = arguments.split(" ")[3];
        
        output.println(Workspaces.addCollaborator(username, collaborator, workspaceName));
    }


    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String arguments) throws IOException {
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
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
        output.println("HAS_PERMS");



        // receive files
        for (int i = 3; i < parts.length; i++) {
            if(fileCoordenator.receive_file("workspaces/" + file_path)){
                output.println("OK");
            }else{
                System.out.println("ERROR");
            }
        }

    }


    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        String username = arguments.split(" ")[1];
        String workspace = arguments.split(" ")[2];
        String file_path = Workspaces.findWorkspace(workspace);
        if (file_path.equals("")) {
            output.println("NOWS");
            return;
        }
        if (!file_path.contains(username)) {
            output.println("NOPERMS");
            return;
        }
        // TODO CHECK IF THE USER CAN ACCESS THE WORKSPACE


        String available_files = "";
        for(int i = 3; i < parts.length; i++){
            if (!(new File(parts[i])).exists()) {
                // System.out.println("File " + parts[i] + " does not exist");
                continue;
            }
            available_files += parts[i] + " ";
        }

        output.println(available_files);
        if(available_files.length() == 0){
            return;
        }
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);

        for (String s : available_files.split(" ")) {
            // System.out.println("File: " + s);
            fileCoordenator.send_file("workspaces/" + file_path + "/" + s);
        }

    }


    // RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.
    public static void rm(String arguments) throws IOException {
        String[] parts = arguments.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid number of arguments");
            return;
        }
        String username = arguments.split(" ")[1];
        String workspace = arguments.split(" ")[2];

        String file_path = Workspaces.findWorkspace(workspace);
        if (file_path.equals("")) {
            output.println("NOWS");
            return;
        }
        if (!file_path.contains(username)) {
            output.println("NOPERMS");
            return;
        }

        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (int i = 3; i < parts.length; i++) {
            System.out.print("File: " + parts[i] + "");
            output.println(fileCoordenator.delete_file("workspaces/" + file_path + "/" + parts[i]));
        }

        // output.println("RM " + username + " " + arguments);
    }


    // LW # Lista os workspaces associados ao utilizador.
    public static void lw(String operation) throws IOException {
        String username = operation.split(" ")[1];
        String workspaces = Workspaces.getAllWorkspaces();
        for (String workspace : workspaces.split("\n")) {
            if(Workspaces.hasCollaborator(workspace, username)){
                System.out.println(workspace);
                output.println(workspace);
            }
        }
        output.println("EOF");
    }


    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String arguments) throws IOException {
        String username = arguments.split(" ")[arguments.split(" ").length - 1];
        String workspace = arguments.split(" ")[1];
        output.println(Workspaces.getAllFilesNames(username, workspace));
        output.println("EOF");
    }

}
