package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.WebSocketManager;

public class RestGetSubsCommand implements ConsoleCommand {

    private WebSocketManager webSocketManager;

    public RestGetSubsCommand(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    @Override
    public String command() {
        return "get-subs";
    }

    @Override
    public String executeCommand(String[] args) {
        webSocketManager.sendMessage("get");
        return "List of subscribed restaurant ids:";
    }
}
