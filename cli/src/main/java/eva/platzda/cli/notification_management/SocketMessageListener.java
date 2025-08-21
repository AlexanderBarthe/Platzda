package eva.platzda.cli.notification_management;

public interface SocketMessageListener {

    void notify(String line);

    long getNotificationId();

    void setNotificationId(long notificationId);

    String getNotificationType();
}
