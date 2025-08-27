package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SubscriptionService;
import eva.platzda.cli.notification_management.receivers.RestaurantSubscriber;

public class RestSubscribeCommand implements ConsoleCommand {

    
    private final SubscriptionService subscriptionService;

    public RestSubscribeCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command() {
        return "sub";
    }

    @Override
    public String executeCommand(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Not enough arguments provided. See 'help rest' for more information.");
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }

        return subscriptionService.subscribeToObject(new RestaurantSubscriber(id, System.out::println));

    }
}
