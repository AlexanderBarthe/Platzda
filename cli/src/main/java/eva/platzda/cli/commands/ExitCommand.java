package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SocketManager;

public class ExitCommand implements ConsoleCommand {

    private SocketManager socketManager;

    public ExitCommand(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    @Override
    public String command() {
        return "exit";
    }

    @Override
    public String executeCommand(String[] args) {

        socketManager.disconnect();
        socketManager.shutdown();

        System.out.println("Goodbye!");
        System.exit(0);
        return null;

    }
}
