package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.user_sub.*;

public class UserCommand extends CommandExecutor implements ConsoleCommand {

    public UserCommand() {
        super(new UserListCommand(),
                new UserCreateCommand(),
                new UserGetCommand(),
                new UserEditCommand(),
                new UserDeleteCommand(),
                new UserDropCommand(),
                new UserFlagCommand(),
                new UserUnflagCommand()
        );
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
