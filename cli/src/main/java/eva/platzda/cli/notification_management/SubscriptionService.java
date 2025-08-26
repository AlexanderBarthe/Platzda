package eva.platzda.cli.notification_management;

import eva.platzda.cli.notification_management.receivers.AnswerReceiver;
import eva.platzda.cli.notification_management.receivers.NotificationReceiver;
import eva.platzda.cli.notification_management.receivers.SocketNotificationType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class SubscriptionService implements SocketNotificationReceiver {
    
    private final Set<NotificationReceiver> notificationReceivers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final SocketManager socketManager;


    private static final int TIMEOUT = 10;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    
    public SubscriptionService() {
        socketManager = new SocketManager(this);
    }


    public String subscribeToObject(NotificationReceiver subscriber) {
        this.notificationReceivers.add(subscriber);
        return sendObjectSubscription(subscriber);
    }

    private String sendObjectSubscription(NotificationReceiver subscriber) {
        try {
            //Action, Type, Id
            return sendAndAwait("subscribe;" + subscriber.getNotificationType().getTranslation() + ";" + subscriber.getNotificationId());
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String unsubscribeFromObject(SocketNotificationType type, Long notificationId) {
        List<NotificationReceiver> toRemove = notificationReceivers.stream()
                .filter(nr -> nr.getNotificationType() == type)
                .filter(nr -> nr.getNotificationId() == notificationId)
                .toList();
        toRemove.forEach(notificationReceivers::remove);
        try {
            //Action, Type, Id
            return sendAndAwait("unsubscribe;" + type.getTranslation() + ";" + notificationId);
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String unsubscribeFromObject(NotificationReceiver subscriber) {
        this.notificationReceivers.remove(subscriber);
        try {
            //Action, Type, Id
            return sendAndAwait("unsubscribe;" + subscriber.getNotificationType().getTranslation() + ";" + subscriber.getNotificationId());
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String unsubscribeAllOfType(SocketNotificationType type) {
        Set<NotificationReceiver> toUnsubscribe = notificationReceivers
                .stream()
                .filter(notificationReceiver -> notificationReceiver.getNotificationType() == type)
                .collect(Collectors.toSet());

        for(NotificationReceiver receiver : toUnsubscribe) {
            notificationReceivers.remove(receiver);
        }

        try {
            //Action, Type
            return sendAndAwait("unsubscribe;" + type.getTranslation());
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }

    public String getSubscriptionsOfType(SocketNotificationType type) {
        try {
            //Action, Type
            return sendAndAwait("get;" + type.getTranslation());
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    public void resubscribeAll() {
        Set<NotificationReceiver> subscribedObjectReceivers = notificationReceivers
                .stream()
                .filter(rec -> rec.getNotificationType() == SocketNotificationType.NOTIFICATION_RESERVATION || rec.getNotificationType() == SocketNotificationType.NOTIFICATION_RESTAURANT)
                .collect(Collectors.toSet());

        for(NotificationReceiver subscribedObjectReceiver : subscribedObjectReceivers) {
            sendObjectSubscription(subscribedObjectReceiver);
        }

    }

    @Override
    public void sendNotification(String msg) {
        //Type, id, msg
        String[] args = msg.split(";");
        if(args.length < 2) throw new IllegalArgumentException("Not enough arguments in message");

        for(NotificationReceiver subscriber : notificationReceivers) {
            if(subscriber.getNotificationType() == SocketNotificationType.fromString(args[0]) && subscriber.getNotificationId() == Long.parseLong(args[1])) {
                subscriber.notify(args[2]);
            }
        }
    }

    public Long generateUniqueId() {
        Random random = new Random();

        Long result;
        boolean idExists;

        do {
            result = random.nextLong();
            Long finalResult = result;
            idExists = notificationReceivers.stream().filter(nr -> nr.getNotificationType() == SocketNotificationType.ANSWER).anyMatch(nr -> nr.getNotificationId() == finalResult);

        } while (idExists);

        return result;
    }


    private String sendAndAwait(String message) throws Exception {

        CompletableFuture<String> future = new CompletableFuture<>();

        Long answerId = generateUniqueId();

        NotificationReceiver answerListener = new AnswerReceiver(answerId, line -> {
            if (line == null) return;

            if ("__CONNECTION_CLOSED__".equals(line)) {
                future.completeExceptionally(new IllegalStateException("Connection closed"));
                return;
            }

            future.complete(line);
        });

        notificationReceivers.add(answerListener);

        try {
            //ResponseReceiverId,Action,Type,(Id)
            getSocketManager().sendMessage(answerListener.getNotificationId() + ";" + message);
            try {
                return future.get(TIMEOUT, UNIT);
            } catch (TimeoutException te) {
                throw new TimeoutException("Timeout waiting for response to: " + message);
            }
        } finally {
            notificationReceivers.remove(answerListener);
        }
    }

    public SocketManager getSocketManager() {
        return socketManager;
    }

    
}
