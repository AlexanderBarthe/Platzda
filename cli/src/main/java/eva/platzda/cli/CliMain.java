package eva.platzda.cli;

import eva.platzda.cli.commands.execution.ConsoleManager;

public class CliMain {
    public static void main(String[] args) {
        ConsoleManager cm = new ConsoleManager();
        cm.run();

        System.out.println("Goodbye!");
    }
}