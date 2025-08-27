package eva.platzda.cli.notification_management.receivers;

import java.util.function.Consumer;

/**
 *
 * Receiver waiting for Reservation notifications
 *
 */
public class ReservationSubscriber extends NotificationReceiver{

    public ReservationSubscriber(Long reservationId, Consumer<String> action) {
        super(reservationId, SocketNotificationType.NOTIFICATION_RESERVATION, action);
    }
}
