package eva.platzda.cli.notification_management.receivers;

import java.util.function.Consumer;

/**
 *
 * Receiver waiting for direct answers to requests
 *
 */
public class AnswerReceiver extends NotificationReceiver {

    public AnswerReceiver(Long id, Consumer<String> action) {

        super(id, SocketNotificationType.ANSWER, action);

    }

}
