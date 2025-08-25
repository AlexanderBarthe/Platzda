package eva.platzda.cli.commands.rest_sub;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

public class RestSearchCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "search";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {
        if(args.length < 1) throw new IllegalArgumentException("No searched tags specified");

        String searchedTags = String.join(" ", args);

        String json = "{\"string\":\"" + searchedTags + "\"}";

        return RestClient.sendRequest("restaurants/search", HttpMethod.POST, json);


    }
}
