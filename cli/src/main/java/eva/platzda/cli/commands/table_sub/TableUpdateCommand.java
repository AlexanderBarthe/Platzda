package eva.platzda.cli.commands.table_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class TableUpdateCommand implements ConsoleCommand {

    @Override
    public String command(){return "update";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help table' for more information.";
        }
        Long tableId;
        Long restaurantId;
        int size;
        try {
            tableId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid table id.");
        }
        try {
            restaurantId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        try {
            size = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size.");
        }

        String json = "{\"id\": " + tableId +","+
                "\"restaurantId\":" + restaurantId + ","+
                "\"size\":" + size +
                "}";

        return RestClient.sendRequest("table/"+tableId, HttpMethod.PUT, json);
    }
}
