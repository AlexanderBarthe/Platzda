package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.util.Arrays;

public class RestUntagCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "untag";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 1){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        long restaurantId;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid restaurant ID.";
        }

        String tag = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        String json = "{ \"tag\": \"" + tag + "\" }";

        return RestClient.sendRequest("restaurants/" + restaurantId + "/untag", HttpMethod.PUT, json);

    }
}
