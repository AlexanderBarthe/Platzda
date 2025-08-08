package eva.platzda.cli;

import eva.platzda.cli.commands.ConsoleCommand;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleManager {

    private final HashMap<String, ConsoleCommand> commands = new HashMap<>();

    public ConsoleManager() {
        registerCommands();
    }

    public void run() {

        Scanner input = new Scanner(System.in);

        while (true) {

            String[] nextArgs =  input.nextLine().split(" ");

            ConsoleCommand command = commands.get(nextArgs[0]);

            if(command == null) {
                System.out.println("Invalid command");
                continue;
            }

            command.execute(nextArgs);

        }


    }

    private void registerCommands() {


    }

    private void register(ConsoleCommand  command) {
        commands.put(command.command(), command);
    }
}
