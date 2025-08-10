package eva.platzda.backend.core.websockets;

import eva.platzda.backend.core.services.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class TableUpdateWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> subscriptions = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TableUpdateWebSocketHandler.class);

    private final RestaurantService restaurantService;

    private static final String CMD_SUBSCRIBE = "subscribe";
    private static final String CMD_UNSUBSCRIBE = "unsubscribe";
    private static final String CMD_GET = "get";

    @Autowired
    public TableUpdateWebSocketHandler(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("WebSocket connection established: {}", session.getRemoteAddress());
    }
    

    /**
     *
     * Used to subscribe or unsubscribe from changes to tables of a restaurant
     *
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        logger.debug("Received message from {}: {}", session.getRemoteAddress(), message.getPayload());

        String[] args = Arrays.stream(message.getPayload().split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        if (args.length == 0 || (args.length <= 1 && !args[0].equals(CMD_GET))) {
            sendError(session, "Insufficient arguments");
            logger.warn("Invalid message from {}: Insufficient arguments", session.getRemoteAddress());
            return;
        }
        else if (args.length == 1 && args[0].equals(CMD_GET)) {

            String ids = subscriptions.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(session))
                    .map(entry -> entry.getKey().toString())
                    .collect(Collectors.joining(","));

            sendPlainMessage(session, ids);
            return;
        }

        long id;
        try {
            id = Long.parseLong(args[1]);
        } catch (Exception ex) {
            sendError(session, "Invalid Id: " + args[1]);
            logger.warn("Invalid ID from {}: {}", session.getRemoteAddress(), args[1]);
            return;
        }
        if(restaurantService.findById(id) == null) {
            sendError(session, "Restaurant not found with id: " + args[1]);
            logger.warn("Unknown ID from {}: {}", session.getRemoteAddress(), args[1]);
            return;
        }

        switch(args[0]) {
            case CMD_SUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> new HashSet<>()).add(session);
                sendSuccess(session, "Subscribed to restaurant with id " + id);
                logger.info("Session {} subscribed to restaurant {}", session.getRemoteAddress(), id);
            }
            case CMD_UNSUBSCRIBE -> {
                subscriptions.computeIfAbsent(id, k -> new HashSet<>()).remove(session);
                sendSuccess(session, "Unsubscribed from restaurant with id " + id);
                logger.info("Session {} unsubscribed from restaurant {}", session.getRemoteAddress(), id);
            }
            default -> {
                sendError(session, "Unknown operator '" +  args[0] + "'");
                logger.warn("Invalid command from {}: {}", session.getRemoteAddress(), args[0]);
            }
        }

    }

    /**
     *
     * Removes connection from list on close.
     *
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {}", session.getRemoteAddress());
        subscriptions.values().forEach(sessions -> sessions.remove(session));
    }

    /**
     *
     * Broadcasts changes to a specific restaurant for subscribed group
     *
     */
    public void broadcastChange(Long restaurantId, String updateMessage) throws Exception {
        Set<WebSocketSession> sessions = subscriptions.getOrDefault(restaurantId, Collections.emptySet());
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(updateMessage));
            }
        }
    }


    private void sendError(WebSocketSession session, String msg) throws IOException {
        session.sendMessage(new TextMessage("Error: " + msg));
    }

    private void sendSuccess(WebSocketSession session, String msg) throws IOException {
        session.sendMessage(new TextMessage("Success: " + msg));
    }

    private void sendPlainMessage(WebSocketSession session, String msg) throws IOException {
        session.sendMessage(new TextMessage(msg));
    }

}
