package eva.platzda.cli.commands.hours_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalTime;

public class HoursUpdateCommand implements ConsoleCommand {

    @Override
    public String command(){return "update";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length < 3){
            return "Not enough arguments provided. See 'help hours' for more information.";
        }
        Long id;
        LocalTime openingTime;
        LocalTime closingTime;

        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hours id.");
        }
        try {
            openingTime = LocalTime.parse(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid opening Time.");
        }
        try {
            closingTime = LocalTime.parse(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid closing Time.");
        }

        String json = "{\"id\":" + id + ","+
                "\"openingTime\":\"" + openingTime + "\","+
                "\"closingTime\":\"" + closingTime + "\"" +
                "}";
        return RestClient.sendRequest("hours", HttpMethod.PUT, json);
    }
}
