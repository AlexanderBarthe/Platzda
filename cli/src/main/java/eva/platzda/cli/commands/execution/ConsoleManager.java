package eva.platzda.cli.commands.execution;

import eva.platzda.cli.commands.*;
import eva.platzda.cli.websockets.SubscriptionService;

import java.util.Scanner;

public class ConsoleManager extends CommandExecutor {

    public ConsoleManager(SubscriptionService subscriptionService) {
        super(new HelpCommand(),
                new ExitCommand(subscriptionService.getSocketManager()),
                new UserCommand(),
                new RestCommand(subscriptionService),
                new RunCommand(subscriptionService),
                new TimeCommand(subscriptionService)
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
