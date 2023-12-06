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
            boolean connected = false;
            
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

            connected = true;
            
            while (connected) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine();
                writer.println(command);

                String response;
                try {
                    response = reader.readLine();
                    if (response == null) {
                        System.out.println("Error: There is no connection to the server");
                        connected = false;
                    } else {
                        System.out.println("Server: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Error: There is no connection to the server");
                    connected = false;
                }

                if (command.equalsIgnoreCase("/leave")) {
                    connected = false;
                }
                else if (command.equalsIgnoreCase("/?")) {
                    System.out.println("Available Commands:\n/join <server_ip_add> <port>\n/leave\n/register <handle>\n/store <filename>\n/dir\n/get <filename>\n/?");
                    continue;
                }
                else if (command.equalsIgnoreCase("/store")) {
                    System.out.print("Enter the file path to upload: ");
                    String filePath = scanner.nextLine();
            
                    File file = new File(filePath);
                    if (file.exists()) {
                        // Send /store command to the server
                        writer.println(command);
            
                        // Send the file content to the server
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
            
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                socket.getOutputStream().write(buffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading or sending the file: " + e.getMessage());
                        }
                    } else {
                        System.out.println("File not found!");
                    }
                }
            }

                socket.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}