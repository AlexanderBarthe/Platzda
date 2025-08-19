package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.websockets.SocketManager;

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
        socketManager.sendMessage("get");
        return "List of subscribed restaurant ids:";
    }
}
