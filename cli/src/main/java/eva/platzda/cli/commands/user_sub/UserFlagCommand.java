package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class UserFlagCommand implements ConsoleCommand {

    @Override
    public String command() {
        return "flag";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 1) {
            return "Not enough arguments provided. See 'help user' for more information.";
        }

        long userId;
        long restaurantId;
        try {
            userId = Long.parseLong(args[0]);
            restaurantId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            return "Please enter a valid user ID and restaurant ID.";
        }

        return RestClient.sendRequest("users/flags/" + userId + "/" + restaurantId, HttpMethod.PUT, null);

    }
}
