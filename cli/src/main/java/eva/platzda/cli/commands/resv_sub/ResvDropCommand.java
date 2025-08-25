package eva.platzda.cli.commands.resv_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class ResvDropCommand implements ConsoleCommand {

    @Override
    public String command(){return "drop";}

    @Override
    public String executeCommand(String[] args) {
        return RestClient.sendRequest("reservation", HttpMethod.DELETE, null);
    }
}
