package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.util.Arrays;

public class RestTagCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "tag";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 1){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help rest' for more information.");
        }

        long restaurantId;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }

        String tag = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        String json = "{ \"tag\": \"" + tag + "\" }";

        return RestClient.sendRequest("restaurants/" + restaurantId + "/tag", HttpMethod.PUT, json);

    }
}
