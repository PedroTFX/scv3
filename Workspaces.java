import java.io.File;
import java.util.Arrays;


// workspace1:admin>admin,user1
class Workspaces{
    // make folder in the workspace folder
    public static String create(String Owner, String workspaceName){
        if(existsWorkspace(workspaceName)){
            return "NOK";
        }
        File file = new File("workspaces/" + workspaceName +":"+ Owner + ">" + Owner);
        return file.mkdir() ? "OK" : "NOK";
    }

    public static boolean existsWorkspace(String workspaceName){
        return !findWorkspace(workspaceName).equals("");
    }

    public static boolean hasCollaborator(String fileName, String collaborator){
        return Arrays.stream(fileName.split(">")[1].split(",")).anyMatch(f -> f.equals(collaborator));
        // return fileName.split(">")[1].contains(collaborator);
    }


    public static String addCollaborator(String User, String collaborator, String workspaceName){
        String fileName = findWorkspace(workspaceName);
        if(fileName.equals("")){
            return "NOWS";
        }
        if(!fileName.split(">")[0].contains(User)){
            return "NOPERMS";
        }

        // if the collaborator is already in the workspace
        if(fileName.split(">")[1].contains(collaborator)){
            return "OK";
        }

        // find the workspace folder and add the collaborator
        File file = new File("workspaces/" + fileName);
        File file_renamed = new File("workspaces/" + fileName + "," + collaborator);
        return file.renameTo(file_renamed) ? "OK" : "ERR";

    }


    public static String findWorkspace(String workspaceName){
        File directory = new File("workspaces/");
        // find the workspace folder
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().split(":")[0].equals(workspaceName));;
        return matchingFolders != null && matchingFolders.length > 0 ? matchingFolders[0].getName() : "";
    }

    public static String getAllWorkspaces(){
        File directory = new File("workspaces/");
        File[] matchingFolders = directory.listFiles(File::isDirectory);
        String folderNames = "";
        if (matchingFolders != null && matchingFolders.length > 0) {
            for (File folder : matchingFolders) {
                folderNames += folder.getName() + "\n";
            }
        }
        return folderNames.substring(0, folderNames.length() - 1);
    }

    public static String getAllFilesNames(String User, String workspaceName){
        String fileName = findWorkspace(workspaceName);
        System.out.println("filename" + fileName);
        System.out.println("workspace" + workspaceName);
        if(fileName.equals("")){
            return "NOWS";
        }
        if(!fileName.contains(User)){
            return "NOPERMS";
        }

        // get all files in the workspace
        File[] files = new File("workspaces/" + findWorkspace(workspaceName) + "/").listFiles();
        String fileNames = "";
        if (files != null && files.length > 0) {
            for (File file : files) {
                fileNames += file.getName() + "\n";
            }
        }
        return fileNames.length() > 0 ? fileNames.substring(0, fileNames.length() - 1) : "EMPTY";
    }

    

    public static void main(String[] args){
        Workspaces ws = new Workspaces();
        // full test the class functions
        System.out.println(ws.existsWorkspace("workspace1"));      // false
        System.out.println(ws.create("admin", "workspace1"));      // OK
        System.out.println(ws.existsWorkspace("workspace1"));      // true
        System.out.println(ws.create("admin", "workspace1"));      // NOK

        String fileName = ws.findWorkspace("workspace1");
        System.out.println(ws.hasCollaborator(fileName, "user1"));              // false
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK

        fileName = ws.findWorkspace("workspace1");
        System.out.println(ws.hasCollaborator(fileName, "user1"));              // true
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK

        System.out.println(ws.getAllWorkspaces());                          // workspace1:admin>admin,user1
        System.out.println(ws.getAllFilesNames("admin", "workspace1"));     // EMPTY

        System.out.println(getAllFilesNames("admin", "ROOM"));
        System.out.println(getAllFilesNames("user1", "ROOM"));
        System.out.println(getAllFilesNames("admin", "ROOM1"));

    }
}