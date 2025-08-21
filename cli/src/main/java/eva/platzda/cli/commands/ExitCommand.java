package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SocketManager;
import eva.platzda.cli.notification_management.SubscriptionService;

public class ExitCommand implements ConsoleCommand {

    private SocketManager socketManager;

    public ExitCommand(SubscriptionService subscriptionService) {
        this.socketManager = subscriptionService.getSocketManager();
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
