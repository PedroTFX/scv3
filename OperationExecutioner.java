import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;




public class OperationExecutioner {

    public static PrintWriter output;
    public static BufferedReader input;
    public static InputStream inStream;
    public static OutputStream outStream;
    public static String password;

    public static void execute(String operation, PrintWriter out, BufferedReader in, InputStream is, OutputStream os, String pass) throws Exception {
        password = pass;
        output = out;
        input = in;
        inStream = is;
        outStream = os;
        String operationCommand = operation.split(" ")[0];
        System.out.println("Operation: " + operation);

        // TODO: ADICIONA UMA ROTINA QUE EVIA A TRUSTSTORE MAIS RECENTE PARA O USER QND REQUESITADO

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
    public static void create(String arguments) throws Exception {
        if (arguments == null || arguments.length() == 4) {
            System.out.println("Invalid number of arguments");
            return;
        }

        String username = arguments.split(" ")[1];
        String workspaceName = arguments.split(" ")[2];
        String password = arguments.split(" ")[3];
        
        

        // return routine
        String result = Workspaces.create(username, workspaceName);
        if(result.equals("OK")){
            System.out.println("createWorkspace_MAC: " + MACChecker.createMacWorkspace(username, workspaceName));
            
            // create workspace password
            String key_filename = workspaceName + ".key." + username;
            String workspacePath = "workspaces/" + Workspaces.findWorkspace(workspaceName);
            if(WorkspacePasswordManager.encriptWorkspacePassword(username, workspacePath + "/" + key_filename, password)){
                System.out.println("Workspace password encrypted");
            }
            
            
            // update the MAC for the new user key file
            MACChecker.updateMAC(workspacePath + "/" + key_filename, "users.txt");
        }
        output.println(result);
    }


    // ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. 
    // A operação ADD só funciona se o utilizador for o Owner do workspace <ws>.
    public static void add(String arguments) throws Exception {
        String[] parts = arguments.split(" ");
        if (parts.length != 4) {
            System.out.println("Invalid number of arguments");
            return;
        }

        String Owner = arguments.split(" ")[1];
        String collaborator = arguments.split(" ")[2];
        String workspaceName = arguments.split(" ")[3];
        String totalNameWorkspace = Workspaces.findWorkspace(workspaceName);

        // return routine
        String result = Workspaces.addCollaborator(Owner, collaborator, workspaceName);
        if(result.equals("OK")){
            System.out.println("addCollaborator_MAC: " + MACChecker.addCollaboratorToMacWorkspace(Owner, collaborator, workspaceName));
        
            // decrypt the workspace password
            String workspace_password = WorkspacePasswordManager.decriptWorkspacePassword(Owner, "workspaces/" + totalNameWorkspace + "," + collaborator + "/" + workspaceName + ".key." + Owner);
            String key_filename = workspaceName + ".key." + collaborator;
            String workspacePath = "workspaces/" + Workspaces.findWorkspace(workspaceName);
            WorkspacePasswordManager.encriptWorkspacePassword(collaborator, workspacePath + "/" + key_filename, workspace_password);

            // update the MAC for the new user key file
            MACChecker.updateMAC(workspacePath + "/" + key_filename, "users.txt");
        }

        output.println(result);
    }


    // UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.
    public static void up(String arguments) throws Exception {
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

        // send workspace password of the user
        fileCoordenator.send_file("workspaces/" + file_path + "/" + workspace + ".key." + user);


        // TODO: HOLY SHIT THIS EXTENSIONS ARE A PAIN IN THE ASS, COORDENATE WITH UP IN MENU.JAVA
        // receive files
        for (int i = 3; i < parts.length; i++) {
            System.out.println("File: " + parts[i]);
            // get enc file
            if(fileCoordenator.receive_file("workspaces/" + file_path)){
                output.println("OK");
                MACChecker.updateMAC("workspaces/" + file_path + "/" + parts[i] + ".enc", password);
                System.out.println("File: " + parts[i] + " received");
                
                // get signed file
                if(fileCoordenator.receive_file("workspaces/" + file_path)){
                    output.println("OK");
                    MACChecker.updateMAC("workspaces/" + file_path + "/" + parts[i] + ".signed." + user, password);
                    System.out.println("File: " + parts[i] + ".signed." + user + " received");
                }

            }else{
                System.out.println("ERROR");
            }
        }
    }


    // DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local.
    public static void dw(String arguments) throws Exception {
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
        FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);

        // check if files AND signed files are available
        String files_available = "";
        for (int i = 3; i < parts.length; i++){
            if(!FileCoordenator.isFileInFolder(parts[i] + ".enc", "workspaces/" + file_path)){
                continue;
            }


            String signed_file = parts[i] + ".signed.";
            // System.out.println(signed_file);
            // System.out.println(parts[i]);
            if(FileCoordenator.isFileInFolder(signed_file, "workspaces/" + file_path)){                
                // its guaranteed that the signed file is in the folder
                File[] files = new File("workspaces/" + file_path + "/").listFiles();
                for (File file : files) {
                    if (file.getName().contains(signed_file)) {
                        signed_file = file.getName();
                        break;
                    }
                }


                files_available += parts[i] + ".enc ";
                files_available += signed_file + " ";
            }
        }

        output.println(files_available);
        System.out.println("Files available: " + files_available);
        if(files_available.length() == 0){
            System.out.println("No files available");
            return;
        }

        // send workspace password of the user
        String workspace_password_file = "workspaces/" + file_path + "/" + workspace + ".key." + username;
        System.out.println("Sending workspace password file: " + workspace_password_file);
        if(!fileCoordenator.send_file(workspace_password_file)){
            System.out.println("ERROR");
            return;
        }

        // FileCoordenator fileCoordenator = new FileCoordenator(input, output, inStream, outStream);
        for (String file : files_available.split(" ")){
            if(file.equals("")){
                continue;
            }

            // file integraty check
            if(!MACChecker.checkMAC("workspaces/" + file_path + "/" + file, password)){
                System.out.println("MAC check failed for file: " + file);
                continue;
            }
            System.out.println("Sending file: " + file);
            if(fileCoordenator.send_file("workspaces/" + file_path + "/" + file) && input.readLine().equals("OK")){
                System.out.println(file + ": OK");
            }else{
                System.out.println(file + ": ERR");
            }
        }

    }


    // RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.
    public static void rm(String arguments) throws Exception {
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
                MACChecker.updateMAC("workspaces/" + file_path + "/" + parts[i], password);
            }else{
                output.println("O ficheiro " + parts[i] + " não existe no workspace indicado");
            }
        }
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

        ArrayList<String> workspacesWithUser = new ArrayList<String>();
        String[] allWorkspaces = workspaces.split("\n");
        for(int i = 0; i < allWorkspaces.length; i++){
            if(Workspaces.hasCollaborator(allWorkspaces[i], username)){
                workspacesWithUser.add(allWorkspaces[i].split(":")[0]);
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

        output.println("{ " + filesInWorkspace.replaceAll("\n", " ; ").replaceAll("\\.enc", "") + " }");

        // output.println(Workspaces.getAllFilesNames(username, workspace));


    }

}
