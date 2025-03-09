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
        File file = new File(filename);
        DataOutputStream dataOut = new DataOutputStream(outputStream);

        // Send file metadata
        dataOut.writeUTF(file.getName());  // Send filename
        dataOut.writeLong(file.length());  // Send file size
        dataOut.flush();

        // System.out.println("Sending file: " + file.getName() + " with size: " + file.length());

        try (InputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
                // System.out.println("Sent " + bytesRead + " bytes");
            }
        }

        dataOut.flush();
        // System.out.println("File sent successfully\n");
        return true;
    }

    public boolean receive_file(String workspace) throws IOException {
        DataInputStream dataIn = new DataInputStream(inputStream);

        // Receive metadata
        String file_to_receive = dataIn.readUTF();
        long fileSize = dataIn.readLong();
        
        // System.out.println("Receiving file: " + file_to_receive + " with size: " + fileSize);

        File file = new File(workspace + "/" + file_to_receive);
        // System.out.println("File path: " + file.getAbsolutePath());
        // Ensure the directory exists
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.out.println("Failed to create directory: " + parentDir.getAbsolutePath());
            return false;
        }

        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long bytesReceived = 0;
            int bytesRead;

            while (bytesReceived < fileSize) {
                bytesRead = dataIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived));
                if (bytesRead == -1) break;
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
                // System.out.println("Received " + bytesRead + " bytes");
            }

            fileOutputStream.flush();
        }

        // System.out.println("File received successfully\n");
        return true;
    }


    public boolean delete_file(String filename) {
        System.out.println("Deleting file: " + filename);
        File file = new File(filename);
        return file.delete();
    }

}
