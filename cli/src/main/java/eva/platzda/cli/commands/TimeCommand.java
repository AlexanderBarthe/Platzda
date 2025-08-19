package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.SocketManager;

public class TimeCommand implements ConsoleCommand {

    private final SocketManager socketManager;

    public TimeCommand(SocketManager socketManager) {
        this.socketManager = socketManager;
    }


    @Override
    public String command() {
        return "time";
    }

    @Override
    public String executeCommand(String[] args) {

        ConsoleManager executor = new ConsoleManager(socketManager);
        String command = String.join(" ", args);

        long start = System.currentTimeMillis();

        executor.runCommand(command);

        long end = System.currentTimeMillis();
        long time = end - start;

        return String.format("Execution time: %d ms", time);

    }

}
