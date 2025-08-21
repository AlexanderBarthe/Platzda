package eva.platzda.cli.notification_management;

import java.util.function.Consumer;

public class NotificationReceiver implements SocketMessageListener {

    private long notificationId;

    private String notificationType;

    private Consumer<String> consumer;

    public NotificationReceiver(long notificationId, String notificationType, Consumer<String> consumer) {
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

    @Override
    public void setNotificationId(long notificationId) {
        this.notificationId = notificationId;
    }

    @Override
    public String getNotificationType() {
        return notificationType;
    }





}
