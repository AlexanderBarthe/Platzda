package eva.platzda.cli.notification_management.receivers;


import java.util.function.Consumer;

/**
 *
 * General class abled to handle socket notifications
 *
 */
public class NotificationReceiver {

    //Id of notifications (e.g restaurant id)
    private long notificationId;

    //Type / Channel of notifcation (e.g. reservation)
    private SocketNotificationType notificationType;

    //Method to run when notification with fitting id/channel arrives
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
