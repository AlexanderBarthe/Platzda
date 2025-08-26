package eva.platzda.cli.commands.table_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class TableCreateCommand implements ConsoleCommand {

    @Override
    public String command(){return "create";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help table' for more information.";
        }

        Long restaurantId;
        int size;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        try {
            size = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size.");
        }
        String json = "{\"id\": " + null +","+
                "\"restaurantId\":" + restaurantId + ","+
                "\"size\":" + size +
                "}";
        return RestClient.sendRequest("table/"+restaurantId, HttpMethod.POST, json);
    }
}
