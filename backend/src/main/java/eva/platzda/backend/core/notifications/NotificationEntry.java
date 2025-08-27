package eva.platzda.backend.core.notifications;

/**
 *
 * Datapoint to keep track, which client waits for notification on what objects
 *
 */
public class NotificationEntry {

    private NotificationType type;
    private long awaitedId;
    private ClientConnection connection;

    public NotificationEntry(NotificationType type, long awaitedId, ClientConnection connection) {
        this.type = type;
        this.awaitedId = awaitedId;
        this.connection = connection;
    }

    public NotificationType getType() {
        return type;
    }

    public long getAwaitedId() {
        return awaitedId;
    }

    public ClientConnection getConnection() {
        return connection;
    }

}
