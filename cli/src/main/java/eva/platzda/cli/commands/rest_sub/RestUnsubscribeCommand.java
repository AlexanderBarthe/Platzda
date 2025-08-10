package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.WebSocketManager;

public class RestUnsubscribeCommand implements ConsoleCommand {

    private final WebSocketManager webSocketManager;

    public RestUnsubscribeCommand(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    @Override
    public String command() {
        return "unsub";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        try {
            long id = Long.parseLong(args[0]);
            webSocketManager.sendMessage("unsubscribe;" + id);
            return "Request sent!";
        } catch (NumberFormatException e) {
            return "Please enter a valid restaurant ID.";
        }
    }
}
