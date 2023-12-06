import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FileExchangeServer {
    private static final int PORT = 12345; // Server port
    private static final int BUFFER_SIZE = 1024;
    private static Set<String> registeredHandles = new HashSet<>();
    private static Set<Socket> registeredClients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String command;
                while ((command = reader.readLine()) != null) {
                    String response = processCommand(command, clientSocket);
                    writer.println(response);
                
                    if (command.equalsIgnoreCase("/leave")) {
                        System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                        break;
                    }
                }
                

                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String processCommand(String command, Socket clientSocket) {
        String[] tokens = command.split("\\s+");
        String action = tokens[0].toLowerCase();

        // Check if the client is registered
        if (!isClientRegistered(clientSocket) && !action.equals("/register") && !action.equals("/leave")) {
            return "Please register first in order to use other commands. Thank you";
        }

        switch (action) {
            case "/dir": //comment test
                // Logic to list files in the server directory
                File serverDirectory = new File("server_directory");
                File[] files = serverDirectory.listFiles();
                StringBuilder fileList = new StringBuilder("Server Directory:\n");
                if (files != null) {
                    for (File file : files) {
                        fileList.append(file.getName()).append("\n");
                    }
                }
                return fileList.toString();

            case "/get":
                // Logic to fetch a file from the server
                if (tokens.length == 2) {
                    String fileName = tokens[1];
                    File requestedFile = new File("server_directory", fileName);
                    if (requestedFile.exists()) {
                        return "File received from Server: " + fileName;
                    } else {
                        return "Error: File not found in the server.";
                    }
                } else {
                    return "Error: Invalid parameters for /get command.";
                }

            case "/leave":
                return "Connection closed. Thank you!";

            case "/register":
                if (tokens.length == 2) {
                    String handle = tokens[1];
                    if (!registeredHandles.contains(handle)) {
                        registeredHandles.add(handle);
                        registeredClients.add(clientSocket); // register the client socket
                        return "Welcome " + handle + "!";
                    } else {
                        return "Error: Handle or alias already exists.";
                    }
                } else {
                    return "Error: Invalid parameters for /register command.";
                }
            case "/store":
                if (tokens.length == 2) {
                    String fileName = tokens[1];
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream("server_directory/" + fileName);
            
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
            
                        InputStream input = clientSocket.getInputStream();
                        while ((bytesRead = input.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
            
                        fileOutputStream.close();
                        return "File '" + fileName + "' successfully stored on the server.";
                    } catch (IOException e) {
                        return "Error: " + e.getMessage();
                    }
                } else {
                    return "Error: Invalid parameters for /store command.";
                }
            default:
                return "Error: Command not found";
        }
        
    }
    private static boolean isClientRegistered(Socket clientSocket) {
        return registeredClients.contains(clientSocket);
    }
}