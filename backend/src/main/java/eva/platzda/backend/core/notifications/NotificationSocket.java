package eva.platzda.backend.core.notifications;

import eva.platzda.backend.core.models.Reservation;
import eva.platzda.backend.core.models.Restaurant;
import eva.platzda.backend.core.services.ReservationService;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class NotificationSocket implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    private final RestaurantService restaurantService;

    private final LogService logService;

    private static final int PORT = 9090;

    private ServerSocket serverSocket;
    private ExecutorService acceptorExecutor;
    private ExecutorService clientHandlers;

    Set<NotificationEntry> notificationListeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    public NotificationSocket(RestaurantService restaurantService, LogService logService) {
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
        notificationListeners.forEach(set -> set.getConnection().close());
        notificationListeners.clear();

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
                        replyError(-1L, conn, "Server error: " + e.getMessage());
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

        int argLength = args.length;

        Long answerId = 0L;
        try { answerId = Long.parseLong(args[0]); } catch (Exception e) {
            String msg = "Invalid message from " + conn.getRemoteAddress() + ": Invalid answer id";
            logClientRequest(start, msg, false);
        }

        if(argLength < 3) {
            replyError(answerId, conn, "Insufficient arguments");
            String msg = "Invalid message from " + conn.getRemoteAddress() + ": Insufficient arguments";
            logClientRequest(start, msg, false);
        }

        String operation = args[1];

        NotificationType notificationType = NotificationType.fromString(args[2]);
        if (notificationType == null) {
            replyError(answerId, conn, "Invalid notification type: " + args[2]);
            String msg = "Invalid message from " + conn.getRemoteAddress() + ": Invalid notification type";
            logClientRequest(start, msg, false);
        }

        Long notificationId = null;
        try {notificationId = Long.parseLong(args[3]);} catch (Exception ignored) {}

        switch (operation) {
            case "get": {
                String ids = notificationListeners.stream()
                        .filter(nl -> nl.getType() == notificationType)
                        .filter(nl -> nl.getConnection().equals(conn))
                        .map(nl -> nl.getAwaitedId() + "")
                        .collect(Collectors.joining(","));

                replyPlainMessage(answerId, conn, ids);

                String msg = "Session " + conn.getRemoteAddress() + " requested a subscription list.";
                logClientRequest(start, msg, true);
                return;
            }
            case "subscribe": {
                if(notificationType == NotificationType.NOTIFICATION_RESERVATION) {
                    NotificationEntry notif = new NotificationEntry(NotificationType.NOTIFICATION_RESERVATION, notificationId, conn);
                    notificationListeners.add(notif);

                    replySuccess(answerId, conn, "Subscribed to reservation with id " + notificationId);
                    String msg = "Session " + conn.getRemoteAddress() + " subscribed to reservation " + notificationId;
                    logClientRequest(start, msg, true);
                    return;
                }
                else if(notificationType == NotificationType.NOTIFICATION_RESTAURANT) {
                    if (restaurantService.findById(notificationId) == null) {
                        replyError(answerId, conn, "No restaurant with id " + notificationId + " found.");
                        String msg = "Invalid message from " + conn.getRemoteAddress() + ": No restaurant with id " + notificationId + " found.";
                        logClientRequest(start, msg, false);
                        return;
                    }
                    NotificationEntry notif = new NotificationEntry(NotificationType.NOTIFICATION_RESTAURANT, notificationId, conn);
                    notificationListeners.add(notif);

                    replySuccess(answerId, conn, "Subscribed to restaurant with id " + notificationId);
                    String msg = "Session " + conn.getRemoteAddress() + " subscribed to restaurant " + notificationId;
                    logClientRequest(start, msg, true);
                    return;
                }
            }
            case "unsubscribe": {
                if(args.length < 4) {
                    unsubscribeAll(notificationType, conn);

                    String notificationTypeString = notificationType == NotificationType.NOTIFICATION_RESERVATION ? "reservations" : "restaurants";

                    replySuccess(answerId, conn, "Unsubscribed from all " + notificationTypeString);
                    String msg = "Session " + conn.getRemoteAddress() + " unsubscribed from all " + notificationTypeString;
                    logClientRequest(start, msg, true);
                    return;

                }

                if(notificationType == NotificationType.NOTIFICATION_RESERVATION) {
                    Long finalNotificationId = notificationId;
                    List<NotificationEntry> toUnsub = notificationListeners.stream()
                            .filter(nl -> nl.getConnection().equals(conn)
                            && nl.getType() == notificationType
                            && nl.getAwaitedId() == finalNotificationId)
                                    .collect(Collectors.toList());
                    toUnsub.forEach(nl -> notificationListeners.remove(nl));

                    replySuccess(answerId, conn, "Unsubscribed from reservation with id " + notificationId);
                    String msg = "Session " + conn.getRemoteAddress() + " unsubscribed from reservation " + notificationId;
                    logClientRequest(start, msg, true);
                    return;
                }
                else if(notificationType == NotificationType.NOTIFICATION_RESTAURANT) {
                    if (restaurantService.findById(notificationId) == null) {
                        replyError(answerId, conn, "No restaurant with id " + notificationId + " found.");
                        String msg = "Invalid message from " + conn.getRemoteAddress() + ": No restaurant with id " + notificationId + " found.";
                        logClientRequest(start, msg, false);
                        return;
                    }
                    Long finalNotificationId = notificationId;
                    List<NotificationEntry> toUnsub = notificationListeners.stream()
                            .filter(nl -> nl.getConnection().equals(conn)
                                    && nl.getType() == notificationType
                                    && nl.getAwaitedId() == finalNotificationId)
                            .collect(Collectors.toList());
                    toUnsub.forEach(nl -> notificationListeners.remove(nl));

                    replySuccess(answerId, conn, "Unsubscribed from restaurant with id " + notificationId);
                    String msg = "Session " + conn.getRemoteAddress() + " unsubscribed from restaurant " + notificationId;
                    logClientRequest(start, msg, true);
                    return;
                }

            }
            default: {
                replyError(answerId, conn, "Unknown operation: " + operation);
                String msg = "Invalid message from " + conn.getRemoteAddress() + ": Operation " + operation + " doesnt exist.";
                logClientRequest(start, msg, false);
            }
        }

    }

    private void unsubscribeAll(NotificationType notificationType, ClientConnection conn) {
        List<NotificationEntry> toUnsub = notificationListeners.stream().filter(nl -> nl.getType() == notificationType && nl.getConnection() == conn).collect(Collectors.toList());
        toUnsub.forEach(nl -> notificationListeners.remove(nl));
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
        List<NotificationEntry> toRemove = notificationListeners.stream().filter(nl -> nl.getConnection().equals(conn)).collect(Collectors.toList());
        toRemove.forEach(notificationListeners::remove);
    }

    public void notifyChange(Reservation reservation, String updateMessage) {

        if(reservation == null) return;

        Long changedReservationId = reservation.getId();
        Long restaurantId = reservation.getRestaurantTable().getRestaurant().getId();


        notificationListeners.forEach(ne -> {

            NotificationType listenerNotificationType = ne.getType();
            String notifTypeString = listenerNotificationType.getTranslation();

            Long properIdToSend = ne.getType() == NotificationType.NOTIFICATION_RESTAURANT ? restaurantId : changedReservationId;

            if(ne.getAwaitedId() == properIdToSend) {
                ClientConnection session = ne.getConnection();
                if(session.isOpen()) {
                    try {
                        session.send(notifTypeString + ";" + properIdToSend + ";" + updateMessage);
                    } catch (IOException e) {
                        logger.warn("Failed to send message to {}: {}", session.getRemoteAddress(), e.getMessage());
                    }
                }
            }

        });

    }

    private void replyError(Long id, ClientConnection session, String msg) throws IOException {
        session.send("answer;" + id + ";" + "Error: " + msg);
    }

    private void replySuccess(Long id, ClientConnection session, String msg) throws IOException {
        session.send("answer;" + id + ";" + "Success: " + msg);
    }

    private void replyPlainMessage(Long id, ClientConnection session, String msg) throws IOException {
        session.send("answer;" + id + ";" + msg);
    }

}
