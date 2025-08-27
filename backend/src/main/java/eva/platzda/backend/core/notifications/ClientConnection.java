package eva.platzda.backend.core.notifications;

import java.io.*;
import java.net.Socket;

/**
 *  Helper class to handle multiple clients separately
 */
public class ClientConnection {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     *
     * Reads line from client message
     *
     * @return
     */
    String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     *
     * Sends message to client
     *
     * @param msg
     * @throws IOException
     */
    void send(String msg) throws IOException {
        if (socket.isClosed()) throw new IOException("Socket closed");
        writer.println(msg);
        writer.flush();
    }

    /**
     *
     * Returns state of connection
     *
     * @return
     */
    boolean isOpen() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     *
     * Returns address of client
     *
     * @return
     */
    String getRemoteAddress() {
        return socket.getRemoteSocketAddress() != null ? socket.getRemoteSocketAddress().toString() : "unknown";
    }

    /**
     *
     * Closes connection
     *
     */
    void close() {
        try { writer.close(); } catch (Exception ignored) {}
    }

}
