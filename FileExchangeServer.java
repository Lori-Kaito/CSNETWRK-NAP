import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FileExchangeServer {
    private static final int PORT = 12345; // the server port
    private static final int BUFFER_SIZE = 1024; // buffer size
    private static Set<String> registeredHandles = new HashSet<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT); // port of the server
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Handle each client in a separate thread
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
    
            String command;
            while ((command = reader.readLine()) != null) {
                if (clientSocket.isClosed()) {
                    // Check if the socket is closed
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                    break;
                }
                
                String response = processCommand(command, clientSocket);
                if (response == null) {
                    // If response is null, the socket might have been closed
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                    break;
                }
                writer.println(response);
    
                if (command.equalsIgnoreCase("/leave")) {
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                    break;
                }
            }
    
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    private static String processCommand(String command, Socket clientSocket) {
        String[] tokens = command.split("\\s+");
        String action = tokens[0].toLowerCase();
    
        switch (action) {
            case "/dir":
                // shows the files in the server directory
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
                // used to get the specific file that the user wants
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
                return "Connection closed. Thank you!"; //if client wants to leave in the server

            case "/register": //command to register handle or alias from the client
                return registerHandle(tokens);

            // Add logic for other commands (/store, etc.) here

            case "/store":
                try {
                    receiveFile(clientSocket, command);
                    return "File stored successfully."; // Send a success message
                } catch (IOException e) {
                    return "Error storing file: " + e.getMessage(); // Send an error message
                }

            default:
                return "Error: Command not found.";
        }
    }
    
    


    private static String registerHandle(String[] tokens) {
        if (tokens.length == 2) {
            String handle = tokens[1];
            if (!registeredHandles.contains(handle)) {
                registeredHandles.add(handle);
                return "Welcome " + handle + "!";
            } else {
                return "Error: Handle or alias already exists.";
            }
        } else {
            return "Error: Invalid parameters for /register command.";
        }
    }

    private static void receiveFile(Socket socket, String command) throws IOException {
        String[] storeTokens = command.split("\\s+");
        if (storeTokens.length == 2) {
            String fileName = storeTokens[1];
            try (BufferedInputStream fileInputStream = new BufferedInputStream(socket.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream("server_directory/" + fileName)) {
    
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
    
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
    
                // Get the current timestamp
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
    
            }
        } else {
            throw new IOException("Invalid parameters for /store command.");
        }
    }
}
