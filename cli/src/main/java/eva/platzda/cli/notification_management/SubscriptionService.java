package eva.platzda.cli.notification_management;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class SubscriptionService {
    
    private final HashSet<NotificationReceiver> notificationReceivers = new HashSet<>();

    private final SocketManager socketManager;


    private static final int TIMEOUT = 10;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    
    public SubscriptionService() {
        socketManager = new SocketManager(this);
    }

    public void addNotificationReciever(NotificationReceiver notificationReceiver) {
        notificationReceivers.add(notificationReceiver);
    }

    public void removeNotificationReciever(NotificationReceiver notificationReceiver) {
        notificationReceivers.remove(notificationReceiver);
    }

    public void flushNotificationRecievers() {
        this.notificationReceivers.clear();
    }

    public String subscribeToTable(Long subscribedId) {
        return subscribeToTable(new NotificationReceiver(subscribedId, "notification", System.out::println));
    }
    
    public String subscribeToTable(NotificationReceiver subscriber) {
        this.notificationReceivers.add(subscriber);
        
        return sendTableSubscribtion(subscriber);
    }

    private String sendTableSubscribtion(NotificationReceiver subscriber) {
        try {
            return sendAndAwait(generateUniqueId(), "subscribe;" + subscriber.getNotificationId());
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    public String unsubscribeFromTable(Long subscribedId) {

        Set<NotificationReceiver> subscribersWithId = notificationReceivers.stream()
                .filter(notificationReceiver -> notificationReceiver.getNotificationId() == subscribedId)
                .collect(Collectors.toSet());
        for(NotificationReceiver subscriber : subscribersWithId) {
            notificationReceivers.remove(subscriber);
        }

        try {
            return sendAndAwait(generateUniqueId(), "unsubscribe;" + subscribedId);
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        
    }

    public String getAllTableSubscriptions() {
        try {
            String response = sendAndAwait(generateUniqueId(), "get");
            if(response == null || response.isEmpty()) {
                return "No subscriptions found";
            }
            return response;
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public void resubscribeAll() {
        Set<NotificationReceiver> tableNotificationReceivers = notificationReceivers.stream().filter(n -> n.getNotificationType().equals("notification")).collect(Collectors.toSet());

        for(NotificationReceiver tableNR : tableNotificationReceivers) {
            sendTableSubscribtion(tableNR);
        }
    }

    public void notifyNotificationRecievers(Long subscribedId, String notification) {
        for(NotificationReceiver subscriber : notificationReceivers) {
            if(subscribedId.equals(subscriber.getNotificationId())) {
                subscriber.notify(notification);
            }
        }
    }


    private Long generateUniqueId() {
        Random random = new Random();
        Long id;
        do {
            id = Math.abs(random.nextLong());
        } while (isNotificationIdInUse(id));
        return id;
    }

    private boolean isNotificationIdInUse(Long id) {
        for(NotificationReceiver subscriber : notificationReceivers) {
            if(subscriber.getNotificationId() == id) return true;
        }
        return false;
    }
    
    public SocketManager getSocketManager() {
        return socketManager;
    }



    private String sendAndAwait(Long requestId, String message) throws Exception {

        CompletableFuture<String> future = new CompletableFuture<>();

        NotificationReceiver listener = new NotificationReceiver(requestId, "answer", line -> {
            if (line == null) return;

            if ("__CONNECTION_CLOSED__".equals(line)) {
                future.completeExceptionally(new IllegalStateException("Connection closed"));
                return;
            }

            future.complete(line);
        });

        addNotificationReciever(listener);
        try {
            getSocketManager().sendMessage(requestId + ";" + message);
            try {
                return future.get(TIMEOUT, UNIT);
            } catch (TimeoutException te) {
                throw new TimeoutException("Timeout waiting for response to: " + message);
            }
        } finally {
            removeNotificationReciever(listener);
        }
    }

    
}
