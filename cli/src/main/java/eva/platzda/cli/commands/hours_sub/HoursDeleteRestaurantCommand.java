package eva.platzda.cli.commands.hours_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class HoursDeleteRestaurantCommand implements ConsoleCommand {
    @Override
    public String command(){return "delete-restaurant";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help hours' for more information.";
        }

        Long restaurantId;
        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        return RestClient.sendRequest("hours/restaurant/"+restaurantId, HttpMethod.DELETE, null);
    }
}
