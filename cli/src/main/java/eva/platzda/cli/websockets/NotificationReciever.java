package eva.platzda.cli.websockets;

public class NotificationReciever implements SocketMessageListener{

    public NotificationReciever(SocketManager socketManager) {
        socketManager.subscribe(0, this);
    }

    @Override
    public void notify(String line) {
        System.out.println(line);
    }

}
