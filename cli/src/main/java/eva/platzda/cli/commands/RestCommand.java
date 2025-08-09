package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.rest_sub.*;

import java.util.List;

public class RestCommand extends CommandExecutor implements ConsoleCommand {

    public RestCommand() {
        super(List.of(
                new RestListCommand(),
                new RestCreateCommand(),
                new RestGetCommand(),
                new RestEditCommand(),
                new RestCreateCommand(),
                new RestDeleteCommand(),
                new RestDropCommand(),
                new RestTagCommand(),
                new RestUntagCommand()
        ));
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
