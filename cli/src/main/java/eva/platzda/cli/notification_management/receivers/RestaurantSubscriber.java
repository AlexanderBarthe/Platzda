package eva.platzda.cli.notification_management.receivers;

import java.util.function.Consumer;

/**
 *
 * Receiver waiting for Restaurant notifications
 *
 */
public class RestaurantSubscriber extends NotificationReceiver {

    public RestaurantSubscriber(Long restaurantId, Consumer<String> action) {
        super(restaurantId, SocketNotificationType.NOTIFICATION_RESTAURANT, action);
    }


}
