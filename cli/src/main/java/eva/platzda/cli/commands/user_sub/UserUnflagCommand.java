package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class UserUnflagCommand implements ConsoleCommand {

    @Override
    public String command() {
        return "unflag";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 1) {
            throw new IllegalArgumentException("Not enough arguments provided. See 'help user' for more information");
        }

        long userId;
        long restaurantId;
        try {
            userId = Long.parseLong(args[0]);
            restaurantId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID or restaurant ID");
        }

        return RestClient.sendRequest("users/flags/" + userId + "/" + restaurantId, HttpMethod.DELETE, null);

    }
}
