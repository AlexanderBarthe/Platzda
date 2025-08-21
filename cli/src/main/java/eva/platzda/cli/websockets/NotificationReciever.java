package eva.platzda.cli.websockets;

public class NotificationReciever implements SocketMessageListener{

    public NotificationReciever(SubscriptionService subscriptionService) {
        subscriptionService.getSocketManager().subscribe(0, this);
    }

    @Override
    public void notify(String line) {
        System.out.println(line);
    }

}
