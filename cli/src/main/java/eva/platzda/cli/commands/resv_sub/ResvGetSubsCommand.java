package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SubscriptionService;
import eva.platzda.cli.notification_management.receivers.SocketNotificationType;

public class ResvGetSubsCommand implements ConsoleCommand {

    SubscriptionService subscriptionService;

    public ResvGetSubsCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command() {
        return "get-subs";
    }

    @Override
    public String executeCommand(String[] args) {
        return subscriptionService.getSubscriptionsOfType(SocketNotificationType.NOTIFICATION_RESERVATION);
    }
}
