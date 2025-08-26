package eva.platzda.cli.notification_management.receivers;

import java.util.Random;
import java.util.function.Consumer;

public class AnswerReceiver extends NotificationReceiver {

    public AnswerReceiver(Long id, Consumer<String> action) {

        super(id, SocketNotificationType.ANSWER, action);

    }

}
