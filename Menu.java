class Menu{

    public Menu(BufferedReader input, PrintWriter output, String username, String arguments){
        BufferedReader input = input;
        PrintWriter output = output;

        System.out.println("• CREATE <ws> # Criar um novo workspace - utilizador é Owner. ");
        System.out.println("• ADD <user1> <ws> # Adicionar utilizador <user1> ao workspace <ws>. A operação ADD só funciona se o utilizador for o Owner do workspace <ws>. ");
        System.out.println("• UP <ws> <file1> ... <filen> # Adicionar ficheiros ao workspace.  ");
        System.out.println("• DW <ws> <file1> ... <filen> # Download de ficheiros do workspace para a máquina local. ");
        System.out.println("• RM <ws> <file1> ... <filen> # Apagar ficheiros do workspace.  ");
        System.out.println("• LW # Lista os workspaces associados ao utilizador. ");
        System.out.println("• LS <ws> # Lista os ficheiros dentro de um workspace.");
    


        while(true){
            String command = input.readLine();
            
            switch(parts[0]){
                case "CREATE":
                    create(username, arguments);
                    break;
                case "ADD":
                    add(username, arguments);
                    break;
                case "UP":
                    up(username, arguments);
                    break;
                case "DW":
                    dw(username, arguments);
                    break;
                case "RM":
                    rm(username, arguments);
                    break;
                case "LW":
                    lw();
                    break;
                case "LS":
                    ls(username, arguments);
                    break;
                default:
                    System.out.println("Command not found");
            }
        }
    }

    public void create(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("CREATE " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    public void add(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length != 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("ADD " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    public void up(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("UP " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    public void dw(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("DW " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    public void rm(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length < 2){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("RM " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    public void lw(){
        output.println("LW");
        System.out.println(input.readLine());
    }

    public void ls(String username, String arguments){
        String[] parts = arguments.split(" ");
        if(parts.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        output.println("LS " + username + " " + arguments);
        System.out.println(input.readLine());
    }

    
}