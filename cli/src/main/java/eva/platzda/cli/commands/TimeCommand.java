package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.CommandExecutor;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.WebSocketManager;

public class TimeCommand implements ConsoleCommand {

    private final WebSocketManager webSocketManager;

    public TimeCommand(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }


    @Override
    public String command() {
        return "time";
    }

    @Override
    public String executeCommand(String[] args) {

        ConsoleManager executor = new ConsoleManager(webSocketManager);
        String command = String.join(" ", args);

        long start = System.currentTimeMillis();

        executor.runCommand(command);

        long end = System.currentTimeMillis();
        long time = end - start;

        return String.format("Execution time: %d ms", time);



    }
}
