package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.user_sub.*;

import java.util.List;

public class UserCommand extends CommandExecutor implements ConsoleCommand {

    public UserCommand() {
        super(List.of(
                new UserListCommand(),
                new UserCreateCommand(),
                new UserGetCommand(),
                new UserEditCommand(),
                new UserDeleteCommand(),
                new UserDropCommand()
        ));
    }


    @Override
    public String command() {
        return "user";
    }

    @Override
    public String executeCommand(String[] args) {
        return super.execute(args);
    }
}
