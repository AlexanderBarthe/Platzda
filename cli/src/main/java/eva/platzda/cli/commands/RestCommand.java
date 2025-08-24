package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.rest_sub.*;
import eva.platzda.cli.notification_management.SubscriptionService;

public class RestCommand extends CommandExecutor implements ConsoleCommand {

    public RestCommand(SubscriptionService subscriptionService) {
        super(new RestListCommand(),
                new RestCreateCommand(),
                new RestGetCommand(),
                new RestEditCommand(),
                new RestCreateCommand(),
                new RestDeleteCommand(),
                new RestDropCommand(),
                new RestTagCommand(),
                new RestUntagCommand(),
                new RestSubscribeCommand(subscriptionService),
                new RestUnsubscribeCommand(subscriptionService),
                new RestGetSubsCommand(subscriptionService)
        );
    }


    @Override
    public String command() {
        return "rest";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {
        return super.execute(args);
    }
}
