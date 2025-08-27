package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class ResvDeleteIdCommand implements ConsoleCommand {

    @Override
    public String command(){return "delete-id";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length < 1){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help resv' for more information.");
        }

        Long reservationId;

        try {
            reservationId = Long.parseLong(args[0]);
        } catch (NumberFormatException e)  {
            throw new IllegalArgumentException("Invalid reservation ID");
        }
        return RestClient.sendRequest("reservation/id/" + reservationId, HttpMethod.DELETE, null);
    }
}
