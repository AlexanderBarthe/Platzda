package eva.platzda.cli.websockets;

import java.util.function.Consumer;

public class NotificationReciever implements SocketMessageListener {

    private long notificationId;

    private String notificationType;

    private Consumer<String> consumer;

    public NotificationReciever(long notificationId, String notificationType, Consumer<String> consumer) {
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.consumer = consumer;
    }

    @Override
    public void notify(String line) {
        consumer.accept(line);
    }

    @Override
    public long getNotificationId() {
        return notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }



}
