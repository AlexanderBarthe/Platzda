package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class ResvDeleteUserCommand implements ConsoleCommand {

    @Override
    public String command(){return "deleteUser";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help resv' for more information.");
        }

        Long userId;

        try {
            userId = Long.parseLong(args[0]);
            return RestClient.sendRequest("reservation/" + userId, HttpMethod.DELETE, null);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
