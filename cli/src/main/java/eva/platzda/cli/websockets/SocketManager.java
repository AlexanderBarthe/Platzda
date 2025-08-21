package eva.platzda.cli.websockets;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketManager {

    private static final String serverUrl = Dotenv.load().get("backend_subscriptions_address");;

    private Socket socket;

    private PrintWriter writer;
    private BufferedReader reader;
    private Thread readerThread;
    
    private final SubscriptionService subscriptionService;

    private final Map<SocketMessageListener, Long> listeners = new ConcurrentHashMap<>();

    //Reconnect/Scheduler
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "SocketManager-Reconnect");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean(false);
    private ScheduledFuture<?> reconnectFuture;

    //Backoff-Parameter
    private final long initialDelayMs = 250L;
    private final long maxDelayMs = 30_000L;
    private final double backoffMultiplier = 4.0;

    private volatile long currentDelayMs = initialDelayMs;

    //If true -> no auto-reconnect (for manual disconnects)
    private final AtomicBoolean stopReconnect = new AtomicBoolean(false);

    public SocketManager(SubscriptionService subscriptionService) {
        
        this.subscriptionService = subscriptionService;
        
        try {
            connect();
        } catch (Exception e) {
            System.out.println("Initial connect failed: " + e.getMessage());
            scheduleReconnectIfNeeded();
        }
    }

    /**
     * Open a connection
     */
    public synchronized void connect() {
        if (isConnected()) return;

        if (stopReconnect.get()) {
            return;
        }

        boolean ok = tryConnect();
        if (!ok) {
            scheduleReconnectIfNeeded();
        } else {
            //Reset params on successful connection, resubscribe
            currentDelayMs = initialDelayMs;
            cancelScheduledReconnect();
        }
    }

    /**
     * Attempt to open a connection
     */
    private boolean tryConnect() {
        URI uri;
        try {
            uri = new URI(serverUrl);
        } catch (URISyntaxException e) {
            System.out.println("Invalid serverUrl: " + serverUrl + " -> " + e.getMessage());
            return false;
        }

        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("Connected to server.");

            startReaderThread();

            subscriptionService.resubscribeAll();

            return true;
        } catch (IOException ex) {
            System.out.println("Error connecting to " + host + ":" + port + " -> " + ex.getMessage());
            closeSilently();
            return false;
        }
    }

    private void startReaderThread() {
        //Stop old thread if existing
        if (readerThread != null && readerThread.isAlive()) {
            try {
                readerThread.interrupt();
            } catch (Exception ignored) {}
        }

        readerThread = new Thread(() -> {
            try {
                String line;
                while (socket != null && !socket.isClosed() && (line = reader.readLine()) != null) {
                    String[] split = line.split(";");
                    String refactoredMessage = "";
                    if (split.length > 1) {
                        refactoredMessage = String.join(";", Arrays.copyOfRange(split, 1, split.length));
                    }

                    Long requestId = null;
                    try { requestId = Long.parseLong(split[0]); } catch (Exception ignored) {}
                    if (requestId == null) requestId = 0L;

                    for (Map.Entry<SocketMessageListener, Long> entry : listeners.entrySet()) {
                        if (requestId.equals(entry.getValue())) {
                            try { entry.getKey().notify(refactoredMessage); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                String reason = (socket == null || socket.isClosed()) ? "Socket closed" : "Stream ended";
                System.out.println("Connection closed. Reason: " + reason);

                closeSilently();

                //Atempt reconnect if not stopped manually
                if (!stopReconnect.get()) {
                    scheduleReconnectIfNeeded();
                }
            }
        }, "SocketManager-Reader");

        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Schedule a reconnect attempt using exponential backoff (if not already scheduled).
     */
    private void scheduleReconnectIfNeeded() {
        if (reconnectScheduled.compareAndSet(false, true)) {
            reconnectFuture = reconnectExecutor.schedule(() -> {
                try {
                    reconnectScheduled.set(false); // wir führen einmalig aus
                    if (stopReconnect.get()) {
                        return;
                    }
                    System.out.println("Attempting reconnect...");
                    boolean connected = tryConnect();
                    if (!connected) {
                        //Increase backoff
                        currentDelayMs = Math.min(maxDelayMs, (long)(currentDelayMs * backoffMultiplier));
                        //Schedule next attempt
                        reconnectScheduled.set(false);
                        scheduleReconnectIfNeeded();
                    } else {
                        currentDelayMs = initialDelayMs;
                    }
                } catch (Exception e) {
                    System.out.println("Reconnect attempt failed: " + e.getMessage());
                    currentDelayMs = Math.min(maxDelayMs, (long)(currentDelayMs * backoffMultiplier));
                    reconnectScheduled.set(false);
                    scheduleReconnectIfNeeded();
                }
            }, currentDelayMs, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelScheduledReconnect() {
        try {
            if (reconnectFuture != null && !reconnectFuture.isDone()) {
                reconnectFuture.cancel(true);
            }
        } catch (Exception ignored) {}
        reconnectScheduled.set(false);
    }

    public void sendMessage(String message) {
        if (isConnected() && writer != null) {
            writer.println(message);
            writer.flush();
        } else {
            System.out.println("Error: Can't connect to server");
        }
    }

    /**
     * Disconnect from server
     */
    public synchronized void disconnect() {
        //Prevent reconnect attempts
        stopReconnect.set(true);
        cancelScheduledReconnect();

        if (socket != null) {
            try { socket.close(); } catch (Exception ignored) {}
        }
        closeSilently();
    }

    private synchronized void closeSilently() {
        try { if (writer != null) writer.close(); } catch (Exception ignored) {}
        try { if (reader != null) reader.close(); } catch (Exception ignored) {}

        writer = null;
        reader = null;

        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
        socket = null;

        //Interrupt reader thread
        if (readerThread != null && readerThread.isAlive()) {
            try { readerThread.interrupt(); } catch (Exception ignored) {}
            readerThread = null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void subscribe(long id, SocketMessageListener listener) {
        if (listener != null) listeners.put(listener, id);
    }

    public void unsubscribe(SocketMessageListener listener) {
        if (listener != null) listeners.remove(listener);
    }

    /**
     * Cleanup on ending
     */
    public void shutdown() {
        stopReconnect.set(true);
        cancelScheduledReconnect();
        try { reconnectExecutor.shutdownNow(); } catch (Exception ignored) {}
        disconnect();
    }
}
