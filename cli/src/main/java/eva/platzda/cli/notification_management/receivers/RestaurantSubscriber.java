package eva.platzda.cli.notification_management.receivers;

import java.util.Random;
import java.util.function.Consumer;

public class RestaurantSubscriber extends NotificationReceiver {

    public RestaurantSubscriber(Long restaurantId, Consumer<String> action) {
        super(restaurantId, SocketNotificationType.NOTIFICATION_RESTAURANT, action);
    }


}
