package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class UserListCommand implements ConsoleCommand {

    @Override
    public String command() {
        return "list";
    }

    @Override
    public String executeCommand(String[] args) {
        return RestClient.sendRequest("users", HttpMethod.GET, null);
    }
}
