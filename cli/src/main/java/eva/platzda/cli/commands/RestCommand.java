package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.rest_sub.*;
import eva.platzda.cli.websockets.WebSocketManager;

import java.util.List;

public class RestCommand extends CommandExecutor implements ConsoleCommand {

    public RestCommand(WebSocketManager webSocketManager) {
        super(new RestListCommand(),
                new RestCreateCommand(),
                new RestGetCommand(),
                new RestEditCommand(),
                new RestCreateCommand(),
                new RestDeleteCommand(),
                new RestDropCommand(),
                new RestTagCommand(),
                new RestUntagCommand(),
                new RestSubscribeCommand(webSocketManager),
                new RestUnsubscribeCommand(webSocketManager),
                new RestGetSubsCommand(webSocketManager)
        );
    }


    @Override
    public String command() {
        return "rest";
    }

    @Override
    public String executeCommand(String[] args) {
        return super.execute(args);
    }
}
