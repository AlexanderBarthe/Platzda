package eva.platzda.cli.commands.execution;

import eva.platzda.cli.commands.*;
import eva.platzda.cli.websockets.WebSocketManager;

import java.util.List;
import java.util.Scanner;

public class ConsoleManager extends CommandExecutor {

    public ConsoleManager(WebSocketManager webSocketManager) {
        super(new HelpCommand(),
                new ExitCommand(webSocketManager),
                new UserCommand(),
                new RestCommand(webSocketManager),
                new RunCommand(webSocketManager),
                new TimeCommand(webSocketManager)
        );
    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {

            runCommand(input.nextLine());

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void runCommand(String command) {

        String[] nextArgs =  command.split(" ");

        String answer = "";

        try {
            answer = execute(nextArgs);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid command. See 'help' for more information.");
        }

        System.out.println(answer);
    }

}
