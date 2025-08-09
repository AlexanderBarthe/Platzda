package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class RestGetCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "get";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        try {
            long id = Long.parseLong(args[0]);
            return RestClient.sendRequest("restaurants/" + id, HttpMethod.GET, null);
        } catch (NumberFormatException e) {
            return "Please enter a valid restaurant ID.";
        }
    }
}
