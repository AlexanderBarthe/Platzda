package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.util.Arrays;

public class UserEditCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "edit";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length <= 2){
            return "Not enough arguments provided. See 'help user' for more information.";
        }

        long id;
        try {
            id = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            return "Please enter a valid user ID.";
        }

        String key = args[1];
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        String json = "{\n" +
                "\"id\":"  + id + ",\n" +
                "\"" + key + "\":\"" + value + "\"\n" +
                "}";

        return RestClient.sendRequest("users", HttpMethod.PUT, json);

    }
}
