import java.io.File;

class Workspaces{
    // make folder in the workspace folder
    public static String create(String Owner, String workspaceName){
        if(existsWorkspace(workspaceName, Owner)){
            System.out.println("Workspace NOT created: " + workspaceName +":"+ Owner);
            return "NOK";
        }
        File file = new File("workspaces/" + workspaceName +":"+ Owner + ">" + Owner);
        return file.mkdir() ? "OK" : "NOK";
    }

    public static boolean existsWorkspace(String Owner, String workspaceName){
        return !findWorkspace(Owner, workspaceName, true).equals("");
    }

    public static boolean hasCollaborator(String Owner, String workspaceName, String collaborator){
        return findWorkspace(Owner, workspaceName, true).contains(collaborator);
    }

    public static String addCollaborator(String User, String collaborator, String workspaceName){
        if(findWorkspace(User, workspaceName, true).split(">")[0].equals("")){
            // TODO FIX THIS IS NOT COMPARING RIGHT THE WORKSPACE
            if(findWorkspace("", workspaceName, false).equals("")){
                return "NOPERMS";
            }
            return "NOWS";
        }

        if(hasCollaborator(workspaceName, User, collaborator)){
            return "OK";
        }

        File directory = new File("workspaces/");
        // find the workspace folder
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().equals(findWorkspace(User, workspaceName, false)));
        String folderName = "";
        if (matchingFolders != null && matchingFolders.length > 0) {
            for (File folder : matchingFolders) {
                folderName = folder.getName();
                folder.renameTo(new File("workspaces/" + folderName + "," + collaborator));
            }
        }
        folderName = folderName + "," + collaborator;
        return "OK";
    }


    public static String findWorkspace(String Owner, String workspaceName, boolean exactWsAndOwner){
        String partialName = workspaceName +":"+ Owner;
        File directory = new File("workspaces/");
        // find the workspace folder
        File[] matchingFolders = directory.listFiles(file -> file.isDirectory() && file.getName().contains(partialName));
        String folderName = "";
        if (matchingFolders != null && matchingFolders.length > 0) {
            for (File folder : matchingFolders) {
                if(exactWsAndOwner){
                    String temp = folder.getName().split(">")[0];
                    if(!folder.getName().split(">")[0].equals(partialName)){
                        return folder.getName();
                    }
                } else {
                    folderName = folder.getName();
                }
            }
        }
        return folderName;
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
        if(findWorkspace(User, workspaceName, false).split(">")[0].equals("")){
            if(!findWorkspace(workspaceName, "", false).equals("")){
                return "NOPERMS";
            }
            return "NOWS";
        }


        File folder = new File("workspaces/" + findWorkspace(User, workspaceName, false) + "/");
        File[] files = folder.listFiles();
        String fileNames = "";
        if (files != null && files.length > 0) {
            for (File file : files) {
                fileNames += file.getName() + "\n";
            }
        }
        return fileNames.substring(0, fileNames.length() - 1);
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
        System.out.println(ws.create("admin", "workspace1"));

        System.out.println(ws.hasCollaborator("admin", "workspace", "user2"));
        System.out.println(ws.addCollaborator("admin", "user2", "workspace1"));
        System.out.println(ws.addCollaborator("admin", "user2", "workspace1"));
        System.out.println(ws.hasCollaborator("admin", "workspace1", "user2"));
        
        System.out.println(getAllWorkspaces());
        System.out.println(getAllFilesNames("admin", "workspace1"));
    }
}