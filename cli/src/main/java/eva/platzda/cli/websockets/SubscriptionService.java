package eva.platzda.cli.websockets;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SubscriptionService {
    
    private HashSet<Long> subscribedIds = new HashSet<>();

    private final MessageAwaiter awaiter;

    private final SocketManager socketManager;
    
    public SubscriptionService() {
        socketManager = new SocketManager(this);
        awaiter = new MessageAwaiter(socketManager, 10, TimeUnit.SECONDS);
    }
    
    public void clearSubscribedIds() {
        this.subscribedIds.clear();
    }
    
    public String subscribe(Long subscribedId) {
        this.subscribedIds.add(subscribedId);
        
        try {
            return awaiter.sendAndAwait(new Random().nextLong(), "subscribe;" + subscribedId);
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    public String unsubscribe(Long subscribedId) {
        this.subscribedIds.remove(subscribedId);

        try {
            return awaiter.sendAndAwait(new Random().nextLong(), "unsubscribe;" + subscribedId);
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
        
    }

    public String getAllSubscriptions() {
        try {
            String response = awaiter.sendAndAwait(new Random().nextLong(), "get");
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
        for (Long subscribedId : subscribedIds) {
            subscribe(subscribedId);
        }
    }
    
    public SocketManager getSocketManager() {
        return socketManager;
    }
    
    
}
