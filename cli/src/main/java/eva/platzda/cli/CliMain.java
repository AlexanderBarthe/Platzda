package eva.platzda.cli;

import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.WebSocketManager;

public class CliMain {
    public static void main(String[] args) {

        WebSocketManager webSocketManager = new WebSocketManager();

        ConsoleManager cm = new ConsoleManager(webSocketManager);
        cm.run();

        System.out.println("Goodbye!");
    }
}