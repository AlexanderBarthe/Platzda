package eva.platzda.cli.commands.table_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class TableGetCommand implements ConsoleCommand {

    @Override
    public String command(){return "get";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help table' for more information.";
        }

        Long restaurantId;
        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        return RestClient.sendRequest("table/"+restaurantId, HttpMethod.GET, null);
    }
}
