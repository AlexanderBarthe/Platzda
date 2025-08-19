package eva.platzda.cli.websockets;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class MessageAwaiter {

    private final SocketManager socketManager;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    public MessageAwaiter(SocketManager socketManager, long timeout, TimeUnit unit) {
        this.socketManager = Objects.requireNonNull(socketManager);
        this.timeout = timeout;
        this.timeoutUnit = unit;
    }

    public String sendAndAwait(Long requestId, String message) throws Exception {

        CompletableFuture<String> future = new CompletableFuture<>();

        SocketMessageListener listener = line -> {
            if (line == null) return;

            if ("__CONNECTION_CLOSED__".equals(line)) {
                future.completeExceptionally(new IllegalStateException("Connection closed"));
                return;
            }

            future.complete(line);
        };

        socketManager.subscribe(requestId, listener);
        try {
            socketManager.sendMessage(requestId + ";" + message);
            try {
                return future.get(timeout, timeoutUnit);
            } catch (TimeoutException te) {
                throw new TimeoutException("Timeout waiting for response to: " + message);
            }
        } finally {
            socketManager.unsubscribe(listener);
        }
    }
}
