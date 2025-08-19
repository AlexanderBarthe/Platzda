package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.MessageAwaiter;
import eva.platzda.cli.websockets.SocketManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RestGetSubsCommand implements ConsoleCommand {

    private SocketManager socketManager;

    public RestGetSubsCommand(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    @Override
    public String command() {
        return "get-subs";
    }

    @Override
    public String executeCommand(String[] args) {

        MessageAwaiter awaiter = new MessageAwaiter(socketManager, 10, TimeUnit.SECONDS);

        try {
            String response = awaiter.sendAndAwait(new Random().nextLong(), "get");
            if(response == null || response.isEmpty()) {
                return "No subscriptions found";
            }
            return response;
        } catch (TimeoutException te) {
            return "Timeout.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
