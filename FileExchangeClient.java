import java.io.*;
import java.net.*;
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

                while (true) {
                    System.out.print("Enter command: ");
                    String command = scanner.nextLine();
                    writer.println(command);

                    String response = reader.readLine();
                    System.out.println("Server: " + response);

                    if (command.equalsIgnoreCase("/leave")) {
                        break;
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
}