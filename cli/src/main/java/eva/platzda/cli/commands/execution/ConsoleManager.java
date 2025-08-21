package eva.platzda.cli.commands.execution;

import eva.platzda.cli.commands.*;
import eva.platzda.cli.commands.scripts.ListScriptsCommand;
import eva.platzda.cli.commands.scripts.ScriptCommand;
import eva.platzda.cli.commands.scripts.ScriptLoader;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.Scanner;

public class ConsoleManager extends CommandExecutor {

    public ConsoleManager(SubscriptionService subscriptionService, ScriptLoader scriptLoader) {
        super(new HelpCommand(),
                new ExitCommand(subscriptionService),
                new UserCommand(),
                new RestCommand(subscriptionService),
                new RunCommand(subscriptionService, scriptLoader),
                new TimeCommand(subscriptionService, scriptLoader),
                new ListScriptsCommand(scriptLoader),
                new ScriptCommand(subscriptionService, scriptLoader)
        );
    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {
            runCommand(input.nextLine());
        }

    }

    public void runCommand(String command) {

        String[] nextArgs =  command.split(" ");

        String answer = "";

        try {
            answer = execute(nextArgs);
        } catch (IllegalArgumentException e) {
            System.out.println("An error occurred while executing command: " + e.getMessage());
        }

        System.out.println(answer);
    }

}
