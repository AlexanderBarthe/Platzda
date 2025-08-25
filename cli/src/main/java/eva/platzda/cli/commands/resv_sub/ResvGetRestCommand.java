package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalDate;

public class ResvGetRestCommand implements ConsoleCommand {

    @Override
    public String command(){return "get";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help rest' for more information.");
        }
        Long restaurantId;
        LocalDate date;
        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }
        try {
            date = LocalDate.parse(args[1]);
            return RestClient.sendRequest("reservation/restaurant/" + restaurantId + "?day=" + date, HttpMethod.GET, null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid date");
        }


    }
}
