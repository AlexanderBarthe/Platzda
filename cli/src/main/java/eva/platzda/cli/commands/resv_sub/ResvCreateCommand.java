package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalDateTime;

public class ResvCreateCommand implements ConsoleCommand {

    @Override
    public String command(){return "create";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0) {
            return "Not enough arguments provided. See 'help rest' for more information.";
        }
        Long restaurantId;
        Long userId;
        LocalDateTime start;
        int guests;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id");
        }

        try {
            userId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user id");
        }

        try {
            start = LocalDateTime.parse(args[2]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid time");
        }
        try {
            guests = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user id");
        }

        String json = "{}";
        String url = "reservation?restaurantId=" +restaurantId
                + "&userId=" + userId
                + "&start=" + start
                + "&guests=" + guests;
        return RestClient.sendRequest(url, HttpMethod.POST, json);

    }
}
