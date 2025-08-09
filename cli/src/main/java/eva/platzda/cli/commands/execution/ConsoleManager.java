package eva.platzda.cli.commands.execution;

import eva.platzda.cli.commands.ExitCommand;
import eva.platzda.cli.commands.HelpCommand;
import eva.platzda.cli.commands.UserCommand;

import java.util.List;
import java.util.Scanner;

public class ConsoleManager extends CommandExecutor {

    public ConsoleManager() {
        super(List.of(
                new HelpCommand(),
                new ExitCommand(),
                new UserCommand()
        ));
    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {

            String[] nextArgs =  input.nextLine().split(" ");

            String answer = "";

            try {
                answer = execute(nextArgs);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid command. See 'help' for more information.");
            }

            System.out.println(answer);
        }

    }

}
