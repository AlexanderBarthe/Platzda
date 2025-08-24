package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class UserGetCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "get";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help user' for more information.");
        }

        try {
            long id = Long.parseLong(args[0]);
            return RestClient.sendRequest("users/" + id, HttpMethod.GET, null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID");
        }

    }
}
