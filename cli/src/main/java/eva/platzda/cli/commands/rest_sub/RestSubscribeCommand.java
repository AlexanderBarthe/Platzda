package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.MessageAwaiter;
import eva.platzda.cli.websockets.SocketManager;
import eva.platzda.cli.websockets.SocketMessageListener;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RestSubscribeCommand implements ConsoleCommand {

    private final SocketManager socketManager;

    public RestSubscribeCommand(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    @Override
    public String command() {
        return "sub";
    }

    @Override
    public String executeCommand(String[] args) {
        if (args.length == 0) {
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid restaurant ID.";
        }

        MessageAwaiter awaiter = new MessageAwaiter(socketManager, 10, TimeUnit.SECONDS);

        try {
            return awaiter.sendAndAwait(new Random().nextLong(), "subscribe;" + id);
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }
}
