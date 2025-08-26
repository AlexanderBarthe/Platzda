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
                throw new IllegalArgumentException("Unknown option: " + option);
            }
        }

        if(arguments.isEmpty()) arguments.add("all");

        String response;

        String prefix = "";

        switch (arguments.peekFirst()) {
            case "all" -> response = RestClient.sendRequest("logs", HttpMethod.GET, null);
            case "avg-time" -> response = "Server internal average time: " + Float.valueOf(RestClient.sendRequest("logs/avg-time", HttpMethod.GET, null))/1000 + " ms";
            case "med-time" -> response = "Server internal median time: " + Float.valueOf(RestClient.sendRequest("logs/med-time", HttpMethod.GET, null))/1000 + " ms";
            case "max-time" -> response = "Server internal maximum time: " + Float.valueOf(RestClient.sendRequest("logs/max-time", HttpMethod.GET, null))/1000 + " ms";
            case "success" -> {
                response = RestClient.sendRequest("logs/success", HttpMethod.GET, null);
                prefix = "Successful ";
            }
            case "server-error" -> {
                response = RestClient.sendRequest("logs/server-errors", HttpMethod.GET, null);
                prefix = "Server Error ";
            }
            case "client-error" -> {
                response = RestClient.sendRequest("logs/client-errors", HttpMethod.GET, null);
                prefix = "Client Error ";
            }
            case "stats" -> {
                arguments.removeFirst();
                if(arguments.isEmpty()) throw new IllegalArgumentException("Missing endpoint");
                String endpoint = arguments.peekFirst();
                String json = "{\"string\":\"" + endpoint + "\"}";
                response = RestClient.sendRequest("logs/endpoint-ussage", HttpMethod.POST, json);
            }
            case "flush" -> {
                return RestClient.sendRequest("logs", HttpMethod.DELETE, null);
            }
            default -> {
                long id;
                try {
                    id = Long.parseLong(arguments.pollFirst());
                } catch (NumberFormatException e) {throw new IllegalArgumentException("Invalid argment");}
                response = RestClient.sendRequest("logs/" + id, HttpMethod.GET, null);
            }
        }

        if (counting) {
            int elementCount = countTopLevel(response);
            return prefix + "Log Entries: " + elementCount;
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
