import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileCoordenator {
    
    BufferedReader input;
    PrintWriter output;

    public FileCoordenator(BufferedReader input, PrintWriter output){
        this.input = input;
        this.output = output;
    }

    public boolean send_file(String filename) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        output.println(filename); // First line is the file name
        while ((line = br.readLine()) != null) {
            output.println(line);
        }
        br.close();

        return true;
    }

    public boolean receive_file(String workspace) throws IOException {
        String file_to_receive = input.readLine(); // First line is the file name
        File file = new File("workspaces/" + workspace + "/" + file_to_receive);

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {
            // Read and save the file content
            String line = input.readLine();
            while ((line = input.readLine()) != null && !line.equals("EOF")) {
                bw.write(line);
                bw.newLine();
                bw.flush(); // Ensure all data is written to the file
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
        return true;
    }

}
