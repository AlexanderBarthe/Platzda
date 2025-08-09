package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;

public class ExitCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "exit";
    }

    @Override
    public String executeCommand(String[] args) {

        System.out.println("Goodbye!");
        System.exit(0);
        return null;

    }
}
