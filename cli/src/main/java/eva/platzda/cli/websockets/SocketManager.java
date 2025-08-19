package eva.platzda.cli.websockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class SocketManager {

    private static final String serverUrl = "ws://localhost:9090";

    private Socket socket;

    private PrintWriter writer;
    private BufferedReader reader;
    private Thread readerThread;

    public SocketManager() {
        try {
            connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() throws URISyntaxException {
        URI uri = new URI(serverUrl);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        try {
            socket = new Socket(host, port);

            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("Connected to server.");

            readerThread = new Thread(() -> {
                try {
                    String line;
                    while (!socket.isClosed() && (line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                } finally {
                    String reason = (socket == null || socket.isClosed()) ? "Socket closed" : "Stream ended";
                    System.out.println("Connection closed. Reason: " + reason);
                }
            }, "WebSocketManager-Reader");

            readerThread.setDaemon(true);
            readerThread.start();
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            closeSilently();
        }
    }

    public void sendMessage(String message) {
        if (isConnected() && writer != null) {
            writer.println(message);
            writer.flush();
        } else {
            System.out.println("Error: Can't connect to server");
        }
    }

    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
        closeSilently();
    }

    private void closeSilently() {
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        writer = null;
        reader = null;
        socket = null;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

}
