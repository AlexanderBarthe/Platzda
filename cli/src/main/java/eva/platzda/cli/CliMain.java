package eva.platzda.cli;

import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.commands.scripts.ScriptLoader;
import eva.platzda.cli.notification_management.SubscriptionService;
import io.github.cdimascio.dotenv.Dotenv;

public class CliMain {
    public static void main(String[] args) {

        System.out.println("## Welcome. Use 'help' for list of available commands. ##\n");

        ScriptLoader scriptLoader = new ScriptLoader();
        SubscriptionService subscriptionService = new SubscriptionService();

        ConsoleManager cm = new ConsoleManager(subscriptionService, scriptLoader);
        cm.run();

        System.out.println("Goodbye!");
    }
}