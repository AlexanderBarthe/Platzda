package eva.platzda.cli.commands.table_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class TableDeleteRestaurantCommand implements ConsoleCommand {
    @Override
    public String command(){return "delete-restaurant";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length < 1){
            return "Not enough arguments provided. See 'help table' for more information.";
        }

        Long restaurantId;
        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        return RestClient.sendRequest("table/restaurant/"+restaurantId, HttpMethod.DELETE, null);
    }
}
