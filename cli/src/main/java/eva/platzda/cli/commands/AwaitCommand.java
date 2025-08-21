package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.NotificationReceiver;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AwaitCommand implements ConsoleCommand {

    private final SubscriptionService subscriptionService;

    public AwaitCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command() {
        return "await";
    }

    @Override
    public String executeCommand(String[] args) {

        if(args.length == 0){
            return "Not enough arguments provided. See 'help' for more information.";
        }

        Long notificationId = null;
        try {
            notificationId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid number";
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        NotificationReceiver listener = new NotificationReceiver(notificationId, "notification", line -> {
            if (line == null) return;

            if ("__CONNECTION_CLOSED__".equals(line)) {
                future.completeExceptionally(new IllegalStateException("Connection closed"));
                return;
            }

            future.complete(line);
        });

        subscriptionService.addNotificationReciever(listener);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "Error while waiting for response: " + e.getMessage();
        } finally {
            subscriptionService.removeNotificationReciever(listener);
        }

        return "Notification received.";
    }
}
