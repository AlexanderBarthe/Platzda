package eva.platzda.cli;

import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.SocketManager;

public class CliMain {
    public static void main(String[] args) {

        SocketManager socketManager = new SocketManager();

        ConsoleManager cm = new ConsoleManager(socketManager);
        cm.run();

        System.out.println("Goodbye!");
    }
}