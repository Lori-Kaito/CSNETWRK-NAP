import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileExchangeClient {
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            String serverIP = null;
            int serverPort = 0;

            while (true) {
                System.out.print("Enter server IP address and port (e.g., /join 127.0.0.1 12345): ");
                String joinCommand = scanner.nextLine();
            
                String[] joinTokens = joinCommand.split("\\s+");
                if (joinTokens.length == 3 && joinTokens[0].equalsIgnoreCase("/join")) {
                    serverIP = joinTokens[1];
                    try {
                        serverPort = Integer.parseInt(joinTokens[2]);
                        break; // Successful input, break out of the loop
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid port number!");
                    }
                } else if (joinTokens.length == 1 && joinTokens[0].equalsIgnoreCase("/?")) {
                    System.out.println("Available Commands: /join <server_ip_add> <port>, /?");
                } else {
                    System.out.println("Error: Invalid input format or command. Use /join <server_ip_add> <port> or /? for help.");
                }
            }

                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    System.out.print("Enter command: ");
                    String command = scanner.nextLine();
                    writer.println(command);

                    if (command.equalsIgnoreCase("/leave")) {
                        break;
                    }
                    String[] tokens = command.split("\\s+");
                    if (tokens.length == 2 && tokens[0].equalsIgnoreCase("/store")) {
                        String fileName = tokens[1];
                        File fileToSend = new File(fileName);

                        if (fileToSend.exists()) {
                            try (FileInputStream fileInputStream = new FileInputStream(fileToSend);
                                OutputStream outputStream = socket.getOutputStream()) {

                                byte[] buffer = new byte[BUFFER_SIZE];
                                int bytesRead;

                                // Send command to server
                                writer.println(command);

                                // Send file content to the server
                                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                                outputStream.flush(); // Ensure all data is sent
                                System.out.println("File '" + fileName + "' sent to the server.");
                            } catch (IOException e) {
                                System.err.println("Error sending file: " + e.getMessage());
                            }
                    } else {
                        // Reading and printing server's response for other commands
                        String response;
                        while ((response = reader.readLine()) != null && !response.isEmpty()) {
                            System.out.println("Server: " + response);
                        }
                    }
                }

                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}