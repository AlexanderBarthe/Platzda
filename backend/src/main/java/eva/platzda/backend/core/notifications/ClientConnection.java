package eva.platzda.backend.core.notifications;

import java.io.*;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
    }

    String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    void send(String msg) throws IOException {
        if (socket.isClosed()) throw new IOException("Socket closed");
        writer.println(msg);
        writer.flush();
    }

    boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    String getRemoteAddress() {
        return socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString() : "unknown";
    }

    void close() {
        try { writer.close(); } catch (Exception ignored) {}
    }

}
