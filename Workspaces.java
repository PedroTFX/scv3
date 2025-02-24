import java.io.File;

class Workspaces{
    // make folder in the workspace folder
    public boolean create(String workspaceName, String Owner){
        File file = new File("workspaces/" + workspaceName +":"+ Owner + ">" + Owner);
        if(!file.exists()){
            file.mkdir();
            return true;
        }
        return false;
    }

    public boolean existsWorkspace(String workspaceName){
        File[] files = new File("workspaces/").listFiles();
        for(File file : files){
            if(file.getName().split(":")[0].equals(workspaceName)){
                return true;
            }
        }
        return false;
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
        System.out.println(ws.create("workspace1", "admin"));
        System.out.println(ws.create("workspace1", "admin"));
        System.out.println(ws.existsWorkspace("workspace1"));
        System.out.println(ws.existsWorkspace("workspace2"));


    }
}