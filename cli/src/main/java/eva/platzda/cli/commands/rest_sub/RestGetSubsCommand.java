package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.SubscriptionService;

public class RestGetSubsCommand implements ConsoleCommand {

    SubscriptionService subscriptionService;

    public RestGetSubsCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command() {
        return "get-subs";
    }

    @Override
    public String executeCommand(String[] args) {
        return subscriptionService.getAllSubscriptions();
    }
}
