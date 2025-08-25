package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.hours_sub.*;

public class HoursCommand extends CommandExecutor implements ConsoleCommand {

    public HoursCommand(){
        super(new HoursCreateCommand(),
                new HoursDeleteCommand(),
                new HoursDeleteRestaurantCommand(),
                new HoursDropCommand(),
                new HoursGetCommand(),
                new HoursUpdateCommand());
    }

    @Override
    public String command() {
        return "hours";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {
        return super.execute(args);
    }
}
