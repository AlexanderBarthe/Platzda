package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalDate;

public class ResvGetSlotsCommand implements ConsoleCommand {

    @Override
    public String command(){return "getSlots";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help rest' for more information.");
        }

        Long restaurantId;
        LocalDate day;
        int guests;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant ID");
        }
        try {
            day = LocalDate.parse(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid date");
        }

        try {
            guests = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number of guests");
        }
        String url = "reservation/restaurant/" + restaurantId + "/free-slots?day=" + day + "&guests=" + guests;
        return RestClient.sendRequest(url, HttpMethod.GET, null);

    }
}
