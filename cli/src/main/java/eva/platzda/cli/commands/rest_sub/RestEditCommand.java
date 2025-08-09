package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.util.Arrays;

public class RestEditCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "edit";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 2){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        long restaurant_id;
        try {
            restaurant_id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid restaurant ID.";
        }

        if(args[1].equals("owner")){
            long owner_id;
            try {
                owner_id = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                return "Please enter a valid user ID.";
            }
            return RestClient.sendRequest("restaurants/" +  restaurant_id + "/owner/" + owner_id, HttpMethod.PUT, null);
        }

        String key = args[1];
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        String json = "{\n" +
                "\"id\":"  + restaurant_id + ",\n" +
                "\"" + key + "\":\"" + value + "\"\n" +
                "}";

        return RestClient.sendRequest("restaurants", HttpMethod.PUT, json);

    }
}
