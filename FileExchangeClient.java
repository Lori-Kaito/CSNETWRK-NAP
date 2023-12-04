import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class FileExchangeClient {
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter server IP address and port (e.g., /join 127.0.0.1 8080): ");
            String joinCommand = scanner.nextLine();

            String[] joinTokens = joinCommand.split("\\s+");
            if (joinTokens.length == 3 && joinTokens[0].equalsIgnoreCase("/join")) {
                String serverIP = joinTokens[1];
                int serverPort = Integer.parseInt(joinTokens[2]);

                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                String handle = null; // Initialize handle

                while (true) {
                    System.out.print("Enter command: ");
                    String command = scanner.nextLine();
                    writer.println(command);

                    if (command.toLowerCase().startsWith("/store")) {
                        String[] storeTokens = command.split("\\s+");
                        if (storeTokens.length == 2) {
                            String filePath = storeTokens[1];
                            sendFile(socket, filePath, handle);
                        } else {
                            System.out.println("Error: Invalid parameters for /store command.");
                        }
                    }

                    String response = reader.readLine();
                    System.out.println("Server: " + response);

                    if (response.startsWith("Uploaded")) {
                        System.out.println(response);
                    }

                    if (command.equalsIgnoreCase("/leave")) {
                        break;
                    }

                    // Extract handle from registration response
                    if (command.toLowerCase().startsWith("/register") && response.startsWith("Welcome")) {
                        handle = response.split("\\s+")[1];
                    }
                }

                socket.close();
            } else {
                System.out.println("Invalid join command. Please restart the client and provide a valid /join command.");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void sendFile(Socket socket, String filePath, String handle) throws IOException {
        try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(socket.getOutputStream());
             FileInputStream fileInputStream = new FileInputStream(filePath)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            // Print a message indicating successful file upload
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            System.out.println(handle + " " + timeStamp + ": Uploaded " + filePath);
        }
    }
}
