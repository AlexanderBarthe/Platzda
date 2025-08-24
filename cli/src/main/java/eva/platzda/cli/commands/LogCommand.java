package eva.platzda.cli.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class LogCommand implements ConsoleCommand {
    @Override
    public String command() {
        return "log";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {

        Deque<String> arguments =  new ArrayDeque<>(List.of(args));

        boolean counting = false;

        while (!arguments.isEmpty() && arguments.peekFirst().startsWith("--")) {
            String option = arguments.pollFirst().substring(2);
            if (option.equals("count")) {
                counting = true;
            }
            else {
                return "Unknown option: " + option;
            }
        }

        if(arguments.isEmpty()) arguments.add("all");

        String response;

        switch (arguments.peekFirst()) {
            case "all" -> {
                response = RestClient.sendRequest("logs", HttpMethod.GET, null);
            }
            case "success" -> {
                response = RestClient.sendRequest("logs/success", HttpMethod.GET, null);
            }
            case "server-error" -> {
                response = RestClient.sendRequest("logs/server-errors", HttpMethod.GET, null);
            }
            case "client-error" -> {
                response = RestClient.sendRequest("logs/client-errors", HttpMethod.GET, null);
            }
            case "flush" -> {
                return RestClient.sendRequest("logs", HttpMethod.DELETE, null);
            }
            default -> {
                long id;
                try {
                    id = Long.parseLong(arguments.pollFirst());
                } catch (NumberFormatException e) {return "Invalid argment.";}
                response = RestClient.sendRequest("logs/" + id, HttpMethod.GET, null);
            }
        }

        if (counting) {
            int elementCount = countTopLevel(response);
            return "Log Entries: " + elementCount;
        }

        return response;

    }

    public int countTopLevel(String json) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        if (root.isArray()) {
            return root.size();
        } else if (root.isObject()) {
            return root.size();
        } else {
            return 0;
        }
    }
}
