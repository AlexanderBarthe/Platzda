package eva.platzda.cli.commands.execution;

import eva.platzda.cli.commands.*;
import eva.platzda.cli.websockets.SocketManager;

import java.util.Scanner;

public class ConsoleManager extends CommandExecutor {

    public ConsoleManager(SocketManager socketManager) {
        super(new HelpCommand(),
                new ExitCommand(socketManager),
                new UserCommand(),
                new RestCommand(socketManager),
                new RunCommand(socketManager),
                new TimeCommand(socketManager)
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
