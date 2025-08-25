package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.resv_sub.*;
import eva.platzda.cli.notification_management.SubscriptionService;

public class ResvCommand extends CommandExecutor implements ConsoleCommand {

    public ResvCommand(SubscriptionService subscriptionService){
        super(new ResvCreateCommand(),
                new ResvDeleteIdCommand(),
                new ResvDeleteUserCommand(),
                new ResvDropCommand(),
                new ResvGetRestCommand(),
                new ResvGetSlotsCommand());
    }

    @Override
    public String executeCommand(String[] args) throws Exception {
        return super.execute(args);
    }

    @Override
    public String command() {
        return "resv";
    }
}
