package eva.platzda.cli.commands.hours_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalTime;

public class HoursCreateCommand implements ConsoleCommand {

    @Override
    public String command(){return "create";}

    @Override
    public String executeCommand(String[] args){
        if(args.length < 4){
            return "Not enough arguments provided. See 'help hours' for more information.";
        }

        Long restaurantId;
        int weekday;
        LocalTime openingTime;
        LocalTime closingTime;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id.");
        }
        try {
            weekday = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid weekday.");
        }
        try {
            openingTime = LocalTime.parse(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid opening Time.");
        }
        try {
            closingTime = LocalTime.parse(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid closing Time.");
        }

        String json = "{\"weekday\":" + weekday + ","+
                "\"openingTime\":\"" + openingTime + "\"," +
                "\"closingTime\":\"" + closingTime + "\"" +
                "}";
        return RestClient.sendRequest("hours/"+restaurantId, HttpMethod.POST, json);
    }
}
