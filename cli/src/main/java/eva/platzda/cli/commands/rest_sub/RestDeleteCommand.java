package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class RestDeleteCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "delete";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        try {
            long id = Long.parseLong(args[0]);
            return RestClient.sendRequest("restaurants/" + id, HttpMethod.DELETE, null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
    }
}
