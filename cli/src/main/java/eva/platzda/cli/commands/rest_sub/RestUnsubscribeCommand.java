package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SubscriptionService;

public class RestUnsubscribeCommand implements ConsoleCommand {
    
    private final SubscriptionService subscriptionService;

    public RestUnsubscribeCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command() {
        return "unsub";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help rest' for more information.");
        }

        if(args[0].equals("all")){
            return subscriptionService.unsubscribeFrommAllTables();
        }

        long id = 0;

        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }

        return subscriptionService.unsubscribeFromTable(id);
    }
}
