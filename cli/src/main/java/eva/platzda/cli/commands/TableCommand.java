package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.table_sub.*;

public class TableCommand extends CommandExecutor implements ConsoleCommand {

    public TableCommand(){
        super(new TableCreateCommand(),
                new TableDeleteCommand(),
                new TableDeleteRestaurantCommand(),
                new TableDropCommand(),
                new TableGetCommand(),
                new TableUpdateCommand());
    }

    @Override
    public String command() {
        return "table";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {
        return super.execute(args);
    }
}
