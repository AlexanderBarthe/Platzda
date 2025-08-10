package eva.platzda.cli.websockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketManager {

    private static final String serverUrl =  "ws://localhost:8080/notifications/restaurants";

    private WebSocketClient client;

    public WebSocketManager() {
        try {
            connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connect() throws URISyntaxException {
        client = new WebSocketClient(new URI(serverUrl)) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connected to server.");
            }

            @Override
            public void onMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed. Reason: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        };

        client.connect();
    }

    public void sendMessage(String message) {
        if (client != null && client.isOpen()) {
            client.send(message);
        } else {
            System.out.println("Error: Can't connect to server");
        }
    }

    public void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception ignored) {}
        }
    }

    public boolean isConnected() {
        return client != null && client.isOpen();
    }



}
