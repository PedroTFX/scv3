import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class FileCoordenator {
    
    // BufferedReader input;
    // PrintWriter output;

    // public FileCoordenator(BufferedReader input, PrintWriter output){
    //     this.input = input;
    //     this.output = output;
    // }

    // public boolean send_file(String filename) throws IOException{
    //     BufferedReader br = new BufferedReader(new FileReader(filename));
    //     String line;
    //     output.println(filename); // First line is the file name
    //     while ((line = br.readLine()) != null) {
    //         output.println(line);
    //     }

    //     output.println("EOF");
    //     br.close();

    //     return true;
    // }

    // public boolean receive_file(String workspace) throws IOException {
    //     String file_to_receive = input.readLine(); // First line is the file name
    //     File file = new File("workspaces/" + workspace + "/" + file_to_receive);

    //     try (FileWriter fw = new FileWriter(file);
    //          BufferedWriter bw = new BufferedWriter(fw)) {
    //         // Read and save the file content
    //         String line = null;
    //         while ((line = input.readLine()) != null && !line.equals("EOF")) {
    //             System.out.println(line);
    //             bw.write(line);
    //             bw.newLine();
    //             bw.flush(); // Ensure all data is written to the file
    //         }
    //         bw.close();
    //         fw.close();
    //     } catch (Exception e) {
    //         System.out.println("Error: " + e.getMessage());
    //         return false;
    //     }
    //     return true;
    // }


    BufferedReader input;
    PrintWriter output;
    InputStream inputStream;
    OutputStream outputStream;

    public FileCoordenator(BufferedReader input, PrintWriter output, InputStream inputStream, OutputStream outputStream){
        this.input = input;
        this.output = output;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public boolean send_file(String filename) throws IOException {
        if(filename != null && filename.length() == 0){
            return false;
        }

        File file = new File(filename);
        DataOutputStream dataOut = new DataOutputStream(outputStream);

        // Send file metadata
        dataOut.writeUTF(file.getName());  // Send filename
        dataOut.writeLong(file.length());  // Send file size
        dataOut.flush();

        

        try (InputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }

        dataOut.flush();
        return true;
    }

    public boolean receive_file(String path) throws IOException {
        DataInputStream dataIn = new DataInputStream(inputStream);

        // Receive metadata
        String file_to_receive = dataIn.readUTF();
        long fileSize = dataIn.readLong();
        

        // // file already exists
        // while(isFileInFolder(file_to_receive, path)){
        //     System.out.println(file_to_receive + " : File already exists file will be renamed to: new_" + file_to_receive);
        //     file_to_receive = "new_" + file_to_receive;
        // }

        File file = new File(path + "/" + file_to_receive);

        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long bytesReceived = 0;
            int bytesRead;

            while (bytesReceived < fileSize) {
                bytesRead = dataIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived));
                if (bytesRead == -1) break;
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean delete_file(String filename) {
        System.out.println("Deleting file: " + filename);
        File file = new File(filename);
        return file.delete();
    }

    public static boolean isFileInFolder(String filename, String folder) {
        // System.out.println(folder);
        // System.out.println("/" + Workspaces.findWorkspace(folder) + "/");
        File[] files = new File(folder + "/").listFiles();
        // System.out.println(files.length);
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().contains(filename)) {
                    return true;
                }
            }
        }
        return false;
    }

}
