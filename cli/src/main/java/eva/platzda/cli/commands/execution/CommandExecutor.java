package eva.platzda.cli.commands.execution;

import java.util.Arrays;
import java.util.HashMap;

public abstract class CommandExecutor {

    private final HashMap<String, ConsoleCommand> commandKeys = new HashMap<>();

    public CommandExecutor(ConsoleCommand... commands) {
        registerCommands(commands);
    }

    public String execute(String[] args) throws IllegalArgumentException {
        if(args.length == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }
        if(!commandKeys.containsKey(args[0])) {
            throw new IllegalArgumentException("No such command: " + args[0]);
        }

        if(args.length > 2 && args[1].equals("--silent")) {
            commandKeys.get(args[0]).executeCommand(Arrays.copyOfRange(args, 2, args.length));
            return null;
        }

        return commandKeys.get(args[0]).executeCommand(Arrays.copyOfRange(args, 1, args.length));
    }

    public HashMap<String, ConsoleCommand> getCommandKeys() {
        return commandKeys;
    }

    private void registerCommands(ConsoleCommand... commands) {
        for (ConsoleCommand command : commands) {
            commandKeys.put(command.command(), command);
        }

    }


}
