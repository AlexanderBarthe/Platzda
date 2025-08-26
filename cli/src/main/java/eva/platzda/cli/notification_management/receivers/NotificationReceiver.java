package eva.platzda.cli.notification_management.receivers;


import java.util.function.Consumer;

public class NotificationReceiver {

    private long notificationId;

    private SocketNotificationType notificationType;

    private Consumer<String> consumer;

    public NotificationReceiver(long notificationId, SocketNotificationType notificationType, Consumer<String> consumer) {
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.consumer = consumer;
    }

    public void notify(String line) {
        consumer.accept(line);
    }

    public long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    public SocketNotificationType getNotificationType() {
        return notificationType;
    }



}
