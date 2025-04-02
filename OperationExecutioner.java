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
        // check perms
        String user = parts[1];
        String workspace = parts[2];
        String hasPerms = Workspaces.hasPerms(user, workspace);
        if(!hasPerms.equals("OK")){
            output.println(hasPerms);
            return;
        }
        output.println("HAS_PERMS");
        

        String file_path = Workspaces.findWorkspace(workspace);

        // receive files
        for (int i = 3; i < parts.length; i++) {
            System.out.println("File: " + parts[i]);
            if(fileCoordenator.receive_file("workspaces/" + file_path)){
                output.println("OK");
                System.out.println("File: " + parts[i] + " received");
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
        String hasPerms = Workspaces.hasPerms(username, workspace);
        if(!hasPerms.equals("OK")){
            output.println(hasPerms);
            return;
        }

        String file_path = Workspaces.findWorkspace(workspace);

        // check if files are available
        String files_available = "";
        for (int i = 3; i < parts.length; i++){
            if(FileCoordenator.isFileInFolder(parts[i], "workspaces/" + file_path)){
                files_available += parts[i] + " ";
            }
        }

        output.println(files_available);
        if(files_available.length() == 0){
            System.out.println("No files available");
            return;
        }

        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (String file : files_available.split(" ")){
            if(file.equals("")){
                continue;
            }
            System.out.println("Sending file: " + file);
            if(fileCoordenator.send_file("workspaces/" + file_path + "/" + file) && input.readLine().equals("OK")){
                System.out.println(file + ": OK");
            }else{
                System.out.println(file + ": ERR");
            }
        }

        // output.println("EOF");

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

        // checks
        String file_path = Workspaces.findWorkspace(workspace);
        if (file_path.equals("")) {
            output.println("NOWS");
            return;
        }
        if (!file_path.contains(username)) {
            output.println("NOPERMS");
            return;
        }

        // remove files
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (int i = 3; i < parts.length; i++) {
            System.out.print("File: " + parts[i] + " ");
            if(fileCoordenator.delete_file("workspaces/" + file_path + "/" + parts[i])){
                output.println(parts[i] + ": APAGADO");
            }else{
                output.println("O ficheiro " + parts[i] + " não existe no workspace indicado");
            }
            // output.println(fileCoordenator.delete_file("workspaces/" + file_path + "/" + parts[i]));
        }
        // output.println();
        // output.println("RM " + username + " " + arguments);
    }


    // LW # Lista os workspaces associados ao utilizador.
    public static void lw(String operation) throws IOException {
        String username = operation.split(" ")[1];
        String workspaces = Workspaces.getAllWorkspaces();
        System.out.println(workspaces);
        if(workspaces.equals("EMPTY")){
            output.println(workspaces);
            return;
        }

        String[] workspacesWithUser = new String[workspaces.split("\n").length];
        String[] allWorkspaces = workspaces.split("\n");
        for(int i = 0; i < workspacesWithUser.length; i++){
            if(Workspaces.hasCollaborator(allWorkspaces[i], username)){
                workspacesWithUser[i] = allWorkspaces[i].split(":")[0];
            }
        }

        System.out.println("{ " + String.join(" ; ", workspacesWithUser) + " }");
        output.println("{ " + String.join(" ; ", workspacesWithUser) + " }");
    }


    //  LS <ws> # Lista os ficheiros dentro de um workspace.
    public static void ls(String arguments) throws IOException {
        String username = arguments.split(" ")[1];
        String workspace = arguments.split(" ")[2];

        String filesInWorkspace = Workspaces.getAllFilesNames(username, workspace);
        if(filesInWorkspace.equals("EMPTY")){
            output.println(filesInWorkspace);
            return;
        }

        output.println("{ " + filesInWorkspace.replaceAll("\n", " ; ") + " }");

        // output.println(Workspaces.getAllFilesNames(username, workspace));


    }

}
