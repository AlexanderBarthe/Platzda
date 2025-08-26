package eva.platzda.cli.notification_management.receivers;

import java.util.function.Consumer;

public class ReservationSubscriber extends NotificationReceiver{

    public ReservationSubscriber(Long reservationId, Consumer<String> action) {
        super(reservationId, SocketNotificationType.NOTIFICATION_RESERVATION, action);
    }
}
