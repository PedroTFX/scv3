import java.io.File;


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
        return fileName.split(">")[1].contains(collaborator);
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

        // find the workspace folder
        File directory = new File("workspaces/");
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().equals(fileName));
        
        // add the collaborator to the workspace
        return matchingFolders != null && matchingFolders.length > 0  && matchingFolders[0].renameTo(new File("workspaces/" + fileName + "," + collaborator)) ? "OK" : "ERR";
    }

    // DEPRICATED
    public static String findWorkspace(String Owner, String workspaceName){
        String partialName = workspaceName +":"+ Owner;
        File directory = new File("workspaces/");
        // find the workspace folder
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().contains(partialName));
        String folderName = "";
        if (matchingFolders != null && matchingFolders.length > 0) {
            for (File folder : matchingFolders) {
                folderName = folder.getName();
            }
        }
        return folderName;
    }


    public static String findWorkspace(String workspaceName){
        File directory = new File("workspaces/");
        // find the workspace folder
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().contains(workspaceName));;
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
        if(fileName.equals("")){
            return "NOWS";
        }
        if(!fileName.contains(User)){
            return "NOPERMS";
        }


        File folder = new File("workspaces/" + findWorkspace(User, workspaceName) + "/");
        File[] files = folder.listFiles();
        String fileNames = "";
        if (files != null && files.length > 0) {
            for (File file : files) {
                fileNames += file.getName() + "\n";
            }
        }
        return fileNames.length() > 0 ? fileNames.substring(0, fileNames.length() - 1) : "EMPTY";
    }






    // public boolean isOwner(String workspaceName, String Owner){
    //     File file = new File("workspaces/" + workspaceName +":"+ Owner + ">" + Owner);
    //     return file.exists();
    // }

    // public boolean isCollaborator(String workspaceName, String Owner, String collaborator){
    //     File file = new File("workspaces/" + workspaceName +":"+ Owner + ">" + collaborator);
    //     return file.exists();
    // }
    

    public static void main(String[] args){
        Workspaces ws = new Workspaces();
        // full test the class functions
        System.out.println(ws.existsWorkspace("workspace1"));      // false
        System.out.println(ws.create("admin", "workspace1"));               // OK
        System.out.println(ws.existsWorkspace("workspace1"));      // true
        System.out.println(ws.create("admin", "workspace1"));               // NOK

        String fileName = ws.findWorkspace("workspace1");
        System.out.println(ws.hasCollaborator(fileName, "user1")); // false
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK

        fileName = ws.findWorkspace("workspace1");
        System.out.println(ws.hasCollaborator(fileName, "user1")); // true
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK
        System.out.println(ws.addCollaborator("admin", "user1", "workspace1")); // OK

        System.out.println(ws.getAllWorkspaces());                          // workspace1:admin>admin,user
        System.out.println(ws.getAllFilesNames("admin", "workspace1"));     // EMPTY


    }
}