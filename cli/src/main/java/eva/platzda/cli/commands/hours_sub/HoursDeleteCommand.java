package eva.platzda.cli.commands.hours_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class HoursDeleteCommand implements ConsoleCommand {

    @Override
    public String command(){return "delete";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help rest' for more information.";
        }

        Long hoursId;
        try {
            hoursId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hours id.");
        }
        return RestClient.sendRequest("hours/single/"+hoursId, HttpMethod.DELETE, null);
    }
}
