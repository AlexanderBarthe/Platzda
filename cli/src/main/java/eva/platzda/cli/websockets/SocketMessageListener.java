package eva.platzda.cli.websockets;

public interface SocketMessageListener {

    void notify(String line);

    long getNotificationId();

}
