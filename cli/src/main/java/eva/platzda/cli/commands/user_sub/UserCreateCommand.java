package eva.platzda.cli.commands.user_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class UserCreateCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "create";
    }

    @Override
    public String executeCommand(String[] args) {

        if(args.length == 0){
            return "Not enough arguments provided. See 'help user' for more information.";
        }

        String mergedArgs = String.join(" ", args);
        String[] creds = mergedArgs.split(";");

        String json = "{\n\"name\":\"" + creds[0] + "\"" +
                (creds.length >= 2 ? ",\n\"email\": \"" + creds[1] + "\"" : "")
                + "\n}";
        return RestClient.sendRequest("users", HttpMethod.POST, json);
    }
}
