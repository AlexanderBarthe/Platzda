package eva.platzda.backend.core.sockets;

import eva.platzda.backend.core.services.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class TableUpdateSocketHandler implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TableUpdateSocketHandler.class);

    private final Map<Long, Set<ClientConnection>> subscriptions = new ConcurrentHashMap<>();

    private final RestaurantService restaurantService;

    private static final String CMD_SUBSCRIBE = "subscribe";
    private static final String CMD_UNSUBSCRIBE = "unsubscribe";
    private static final String CMD_GET = "get";

    private static final int PORT = 9090;

    private ServerSocket serverSocket;
    private ExecutorService acceptorExecutor;
    private ExecutorService clientHandlers;

    public TableUpdateSocketHandler(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Override
    public void afterPropertiesSet() {
        startServer();
    }

    @Override
    public void destroy() {
        stopServer();
    }

    private void startServer() {
        acceptorExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TableUpdate-Acceptor");
            t.setDaemon(true);
            return t;
        });
        clientHandlers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "TableUpdate-ClientHandler");
            t.setDaemon(true);
            return t;
        });

        acceptorExecutor.submit(() -> {
            try {
                serverSocket = new ServerSocket();
                //bind on all interfaces
                serverSocket.bind(new InetSocketAddress(PORT));
                logger.info("TCP notification server started on port {}", PORT);

                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    ClientConnection connection = new ClientConnection(socket);
                    clientHandlers.submit(() -> handleClient(connection));
                }
            } catch (IOException e) {
                if (serverSocket != null && serverSocket.isClosed()) {
                    logger.info("Server socket closed, stopping acceptor.");
                } else {
                    logger.error("Error in acceptor thread: {}", e.getMessage(), e);
                }
            }
        });
    }

    private void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        if (acceptorExecutor != null) {
            acceptorExecutor.shutdownNow();
        }
        if (clientHandlers != null) {
            clientHandlers.shutdownNow();
        }

        //Close all client connections
        subscriptions.values().forEach(set -> set.forEach(ClientConnection::close));
        subscriptions.clear();

        logger.info("TCP notification server stopped.");
    }

    private void handleClient(ClientConnection conn) {
        try {
            afterConnectionEstablished(conn);

            String line;
            while (conn.isOpen() && (line = conn.readLine()) != null) {
                try {
                    handleTextMessage(conn, line);
                } catch (Exception e) {
                    logger.warn("Error handling message from {}: {}", conn.getRemoteAddress(), e.getMessage(), e);
                    try {
                        sendError(conn, "Server error: " + e.getMessage());
                    } catch (IOException ignored) {}
                }
            }
        } finally {
            afterConnectionClosed(conn);
            conn.close();
        }
    }

    private void afterConnectionEstablished(ClientConnection conn) {
        logger.info("Socket connection established: {}", conn.getRemoteAddress());
    }

    private void handleTextMessage(ClientConnection conn, String payload) throws Exception {
        logger.debug("Received message from {}: {}", conn.getRemoteAddress(), payload);

        String[] args = Arrays.stream(payload.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (args.length == 0 || (args.length <= 1 && !args[0].equals(CMD_GET))) {
            sendError(conn, "Insufficient arguments");
            logger.warn("Invalid message from {}: Insufficient arguments", conn.getRemoteAddress());
            return;
        } else if (args.length == 1 && args[0].equals(CMD_GET)) {
            //find all ids to which this connection is subscribed
            String ids = subscriptions.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(conn))
                    .map(entry -> entry.getKey().toString())
                    .collect(Collectors.joining(","));
            sendPlainMessage(conn, ids);
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[1]);
        } catch (Exception ex) {
            sendError(conn, "Invalid Id: " + args[1]);
            logger.warn("Invalid ID from {}: {}", conn.getRemoteAddress(), args[1]);
            return;
        }
        if (restaurantService.findById(id) == null) {
            sendError(conn, "Restaurant not found with id: " + args[1]);
            logger.warn("Unknown ID from {}: {}", conn.getRemoteAddress(), args[1]);
            return;
        }

        switch (args[0]) {
            case CMD_SUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(conn);
                sendSuccess(conn, "Subscribed to restaurant with id " + id);
                logger.info("Session {} subscribed to restaurant {}", conn.getRemoteAddress(), id);
            }
            case CMD_UNSUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).remove(conn);
                sendSuccess(conn, "Unsubscribed from restaurant with id " + id);
                logger.info("Session {} unsubscribed from restaurant {}", conn.getRemoteAddress(), id);
            }
            default -> {
                sendError(conn, "Unknown operator '" + args[0] + "'");
                logger.warn("Invalid command from {}: {}", conn.getRemoteAddress(), args[0]);
            }
        }
    }

    private void afterConnectionClosed(ClientConnection conn) {
        logger.info("Socket connection closed: {}", conn.getRemoteAddress());
        subscriptions.values().forEach(set -> set.remove(conn));
    }

    public void broadcastChange(Long restaurantId, String updateMessage) throws Exception {
        Set<ClientConnection> sessions = subscriptions.getOrDefault(restaurantId, Collections.emptySet());
        for (ClientConnection s : sessions.toArray(new ClientConnection[0])) {
            if (s.isOpen()) {
                try {
                    s.send(updateMessage);
                } catch (IOException e) {
                    logger.warn("Failed to send message to {}: {}", s.getRemoteAddress(), e.getMessage());
                }
            }
        }
    }

    private void sendError(ClientConnection session, String msg) throws IOException {
        session.send("Error: " + msg);
    }

    private void sendSuccess(ClientConnection session, String msg) throws IOException {
        session.send("Success: " + msg);
    }

    private void sendPlainMessage(ClientConnection session, String msg) throws IOException {
        session.send(msg);
    }


    private static class ClientConnection {
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
}
