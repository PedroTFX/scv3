class t_authentication{
    // make tests for authentication true and false for every method

    // test for authenticate method
    public static void test_authenticate(){
        assert Authentication.authenticate("admin", "admin") == true;
        assert Authentication.authenticate("admin", "admin1") == false;
    }

    // test for existsUser method
    public static void test_existsUser(){
        assert Authentication.existsUser("admin") == true;
        assert Authentication.existsUser("admin1") == false;
    }

    // test for registerUserInFile method
    public static void test_registerUserInFile(){
        assert Authentication.registerUserInFile("admin", "admin") == false;
        assert Authentication.registerUserInFile("admin1", "admin1") == true;
    }
}