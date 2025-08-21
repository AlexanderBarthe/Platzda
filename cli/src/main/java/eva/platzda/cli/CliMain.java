package eva.platzda.cli;

import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.NotificationReciever;
import eva.platzda.cli.websockets.SubscriptionService;
import eva.platzda.cli.websockets.SocketManager;

public class CliMain {
    public static void main(String[] args) {

        System.out.println("## Welcome. Use 'help' for list of available commands. ##\n");

        SubscriptionService subscriptionService = new SubscriptionService();
        new NotificationReciever(subscriptionService);

        ConsoleManager cm = new ConsoleManager(subscriptionService);
        cm.run();

        System.out.println("Goodbye!");
    }
}