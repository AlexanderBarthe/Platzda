package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalDate;

public class ResvDeleteUserCommand implements ConsoleCommand {

    @Override
    public String command(){return "delete-user";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length < 2){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help resv' for more information.");
        }

        Long userId;
        LocalDate day;

        try {
            userId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        try {
            day = LocalDate.parse(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid date");
        }

        return RestClient.sendRequest("reservation/user/" + userId + "?date=" + day, HttpMethod.DELETE, null);
    }
}
