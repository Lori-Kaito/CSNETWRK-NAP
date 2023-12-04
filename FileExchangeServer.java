import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FileExchangeServer {
    private static final int PORT = 8080; // Server port
    private static Set<String> registeredHandles = new HashSet<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String handle = null; // Initialize handle

                String command;
                while ((command = reader.readLine()) != null) {
                    if (command.toLowerCase().startsWith("/store")) {
                        receiveFile(clientSocket, command, handle);
                    }

                    String response = processCommand(command, handle);
                    writer.println(response);

                    if (response.startsWith("Uploaded")) {
                        System.out.println(response);
                    }

                    if (command.equalsIgnoreCase("/leave")) {
                        System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                        break;
                    }

                    // Extract handle from registration command
                    if (command.toLowerCase().startsWith("/register")) {
                        handle = command.split("\\s+")[1];
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String processCommand(String command, String handle) {
        String[] tokens = command.split("\\s+");
        String action = tokens[0].toLowerCase();

        switch (action) {
            case "/dir":
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
                    String newhandle = tokens[1];
                    if (!registeredHandles.contains(newhandle)) {
                        registeredHandles.add(newhandle);
                        return "Welcome " + handle + "!";
                    } else {
                        return "Error: Handle or alias already exists.";
                    }
                } else {
                    return "Error: Invalid parameters for /register command.";
                }

            // Add logic for other commands (/store, etc.) here

            default:
                return "Error: Command not found.";
        }
    }

    private static void receiveFile(Socket socket, String command, String handle) throws IOException {
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

                // Print a message indicating successful file upload
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                System.out.println(handle + " " + timeStamp + ": Uploaded " + fileName);
            }
        } else {
            System.out.println("Error: Invalid parameters for /store command.");
        }
    }
}
