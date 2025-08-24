package eva.platzda.backend.core.sockets;

import eva.platzda.backend.core.services.RestaurantService;
import eva.platzda.backend.logging.LogService;
import eva.platzda.backend.logging.LoggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class TableUpdateSocketHandler implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(TableUpdateSocketHandler.class);

    private final Map<Long, Set<ClientConnection>> subscriptions = new ConcurrentHashMap<>();

    private final RestaurantService restaurantService;

    private final LogService logService;

    private static final String CMD_SUBSCRIBE = "subscribe";
    private static final String CMD_UNSUBSCRIBE = "unsubscribe";
    private static final String CMD_GET = "get";

    private static final int PORT = 9090;

    private ServerSocket serverSocket;
    private ExecutorService acceptorExecutor;
    private ExecutorService clientHandlers;

    @Autowired
    public TableUpdateSocketHandler(RestaurantService restaurantService, LogService logService) {
        this.restaurantService = restaurantService;
        this.logService = logService;
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
                        sendError(-1L, conn, "Server error: " + e.getMessage());
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
        long start = System.nanoTime();
        logger.debug("Received message from {}: {}", conn.getRemoteAddress(), payload);

        String[] args = Arrays.stream(payload.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        Long requestId = null;
        try {requestId = Long.parseLong(args[0]);} catch (Exception ignored) {}
        if(requestId == null) return;

        if (args.length == 1) {
            //No argument

            sendError(requestId, conn, "Insufficient arguments");
            String msg = "Invalid message from " + conn.getRemoteAddress() + ": Insufficient arguments";
            logClientRequest(start, msg, false);

            return;
        } else if (args.length == 2 && args[1].equals(CMD_GET)) {
            //Return all subscribed ids

            //find all ids to which this connection is subscribed
            String ids = subscriptions.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(conn))
                    .map(entry -> entry.getKey().toString())
                    .collect(Collectors.joining(","));
            sendPlainMessage(requestId, conn, ids);

            String msg = "Session " + conn.getRemoteAddress() + "requested a subscription list.";
            logClientRequest(start, msg, true);
            return;
        } else if (args.length == 2 && args[1].equals(CMD_UNSUBSCRIBE)) {
            //Remove host from all subscriber lists

            subscriptions.forEach((key, value) -> {
                if (value.contains(conn)) {
                    value.remove(conn);
                }
            });
            sendPlainMessage(requestId, conn, "Unsubscribed from all.");


            String msg =  "Session " + conn.getRemoteAddress() + "unsubscribed from all notifications.";
            logClientRequest(start, msg, true);
            return;
        }
        else if (args.length == 2) {
            //Invalid first standalone argument

            sendError(requestId, conn, "Insufficient arguments");
            String msg = "Invalid message from " + conn.getRemoteAddress() + ": Insufficient arguments";
            logClientRequest(start, msg, false);
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[2]);
        } catch (Exception ex) {
            sendError(requestId, conn, "Invalid Id: " + args[2]);

            String msg = "Invalid ID from: " + conn.getRemoteAddress() + ": " + args[2];
            logClientRequest(start, msg, false);
            return;
        }
        if (restaurantService.findById(id) == null) {
            sendError(requestId, conn, "Restaurant not found with id: " + args[2]);
            String msg = "Unknown ID from: " + conn.getRemoteAddress() + ": " + args[2];
            logClientRequest(start, msg, false);
            return;
        }

        switch (args[1]) {
            case CMD_SUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).add(conn);
                sendSuccess(requestId, conn, "Subscribed to restaurant with id " + id);
                String msg = "Session " + conn.getRemoteAddress() + " subscribed to restaurant " + id;
                logClientRequest(start, msg, true);
            }
            case CMD_UNSUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet()).remove(conn);
                sendSuccess(requestId, conn, "Unsubscribed from restaurant with id " + id);
                String msg = "Session " + conn.getRemoteAddress() + " unsubscribed from restaurant " + id;
                logClientRequest(start, msg, true);
            }
            default -> {
                sendError(requestId, conn, "Unknown operator '" + args[1] + "'");
                String msg = "Invalid command from " + conn.getRemoteAddress() + ": " + args[1];
                logClientRequest(start, msg, false);
            }
        }
    }

    private void logClientRequest(Long startTime, String msg, boolean success) {

        long duration = (System.nanoTime() - startTime)/1000;
        if(success) {
            logger.info(msg);
            LoggedEvent loggedEvent = new LoggedEvent("Socket 9090", "Client Request", 200, duration, msg);
            logService.addLoggedEvent(loggedEvent);
        }
        else {
            logger.warn(msg);
            LoggedEvent loggedEvent = new LoggedEvent("Socket 9090", "Client Request", 400, duration, msg);
            logService.addLoggedEvent(loggedEvent);
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
                    s.send(restaurantId + ";" + updateMessage);
                } catch (IOException e) {
                    logger.warn("Failed to send message to {}: {}", s.getRemoteAddress(), e.getMessage());
                }
            }
        }
    }

    private void sendError(Long id, ClientConnection session, String msg) throws IOException {
        session.send(id + ";" + "Error: " + msg);
    }

    private void sendSuccess(Long id, ClientConnection session, String msg) throws IOException {
        session.send(id + ";" + "Success: " + msg);
    }

    private void sendPlainMessage(Long id, ClientConnection session, String msg) throws IOException {
        session.send(id + ";" + msg);
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
