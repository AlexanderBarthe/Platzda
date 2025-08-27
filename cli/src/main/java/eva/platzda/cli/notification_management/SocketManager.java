package eva.platzda.cli.notification_management;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketManager {

    private static final String serverUrl = Dotenv.load().get("backend_subscriptions_address");;

    private Socket socket;

    private PrintWriter writer;
    private BufferedReader reader;
    private Thread readerThread;

    private final SubscriptionService subscriptionService;

    private final Set<SocketNotificationReceiver> notificationHooks = Collections.newSetFromMap(new ConcurrentHashMap<>());

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

        addNotificationHook(subscriptionService);

        try {
            connect();
        } catch (Exception e) {
            System.out.println("Initial connect failed: " + e.getMessage());
            scheduleReconnectIfNeeded();
        }
    }

    /**
     * Open a connection if not already connected and auto-reconnect is allowed.
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
            //Reset params on successful connection
            currentDelayMs = initialDelayMs;
            cancelScheduledReconnect();
        }
    }

    /**
     * Try to establish a TCP connection to the server URL, start reader thread
     * and trigger resubscription. Returns true on success.
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
        int port = uri.getPort() == -1 ? 9090 : uri.getPort();

        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("Connected to server.");

            startReaderThread();

            try {
                subscriptionService.resubscribeAll();
            } catch (Exception e) {
                System.out.println("An error occured trying to resubscribe.");
                e.printStackTrace();
            }

            return true;
        } catch (IOException ex) {
            System.out.println("Error connecting to " + host + ":" + port + " -> " + ex.getMessage());
            closeSilently();
            return false;
        }
    }

    /**
     * Start a daemon thread that reads incoming lines and forwards them to hooks.
     * On error or stream end it will close the connection and schedule reconnects.
     */
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
                    if (line.startsWith("Error:")) throw new Exception(line);

                    for(SocketNotificationReceiver notificationHook : notificationHooks) {
                        notificationHook.sendNotification(line);
                    }

                }
            } catch (Exception e) {
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
     * Schedule a reconnect using exponential backoff if no reconnect is already scheduled.
     */
    private void scheduleReconnectIfNeeded() {
        if (reconnectScheduled.compareAndSet(false, true)) {
            reconnectFuture = reconnectExecutor.schedule(() -> {
                try {
                    reconnectScheduled.set(false);
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


    /**
     * Cancel a pending reconnect attempt if present.
     */
    private void cancelScheduledReconnect() {
        try {
            if (reconnectFuture != null && !reconnectFuture.isDone()) {
                reconnectFuture.cancel(true);
            }
        } catch (Exception ignored) {}
        reconnectScheduled.set(false);
    }

    /**
     * Send a raw message to the server if connected.
     *
     * @param message the full message line to send
     */
    public void sendMessage(String message) {
        if (isConnected() && writer != null) {
            writer.println(message);
            writer.flush();
        } else {
            System.out.println("Error: Can't connect to server");
        }
    }

    /**
     * Disconnect from server. Prevents reconnect.
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

    /**
     * Close streams, socket and reader thread quietly
     */
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


    /**
     * Stop reconnection activity, shut down the executor and disconnect.
     * Intended for full application shutdown.
     */
    public void shutdown() {
        stopReconnect.set(true);
        cancelScheduledReconnect();
        try { reconnectExecutor.shutdownNow(); } catch (Exception ignored) {}
        disconnect();
    }


    /**
     * Register a notification receiver to receive incoming messages.
     *
     * @param notificationHook hook that will receive messages
     */
    public void addNotificationHook(SocketNotificationReceiver notificationHook) {
        notificationHooks.add(notificationHook);
    }


    /**
     * Remove a previously registered notification receiver.
     *
     * @param notificationHook hook to remove
     */
    public void removeNotificationHook(SocketNotificationReceiver notificationHook) {
        notificationHooks.remove(notificationHook);
    }
}
