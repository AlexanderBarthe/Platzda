package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class RestCreateCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "create";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }
        Long userId;

        try {
            userId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid user ID.";
        }

        String json = "{}";
        return RestClient.sendRequest("restaurants/" + userId, HttpMethod.POST, json);
    }
}
