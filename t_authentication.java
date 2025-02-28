

class t_authentication{
    // make tests for authentication true and false for every method

    // test for authenticate method
    public static void test_authenticate(){
        assert Authentication.authenticateUser("admin", "admin") == true;
        assert Authentication.authenticateUser("admin", "admin1") == false;
    }

    // test for existsUser method
    public static void test_existsUser(){
        assert Authentication.existsUser("admin") == true;
        assert Authentication.existsUser("admin1") == false;
    }

    public static void test_removeUser(){
        assert Authentication.registerUserInFile("admin1", "admin1") == true;
        assert Authentication.existsUser("admin1") == true;
        Authentication.removeUser("admin1");
        assert Authentication.existsUser("admin1") == false;
    }

    // test for registerUserInFile method
    public static void test_registerUserInFile(){
        assert Authentication.registerUserInFile("admin", "admin") == false;
        assert Authentication.registerUserInFile("admin1", "admin1") == true;
        assert Authentication.existsUser("admin1") == true;
        Authentication.removeUser("admin1");
    }

    public static void test_auth(){
        assert Authentication.auth("admin", "admin") == "OK-AUTHENTICATED";
        assert Authentication.auth("admin", "admin1") == "WRONG-PWD";
        assert Authentication.auth("admin1", "admin1") == "OK-NEW-USER";
        Authentication.removeUser("admin1");
    }

    public static void test_workspace_creation(){
        Workspaces ws = new Workspaces();
        
    }

    public static void main(String[] args){
        test_authenticate();
        test_existsUser();
        test_registerUserInFile();
        test_removeUser();
        test_workspace_creation();
    }


}