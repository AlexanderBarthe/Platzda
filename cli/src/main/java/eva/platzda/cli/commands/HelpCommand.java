package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;

import java.util.LinkedHashMap;
import java.util.Map;

public class HelpCommand implements ConsoleCommand {

    @Override
    public String command() {
        return "help";
    }

    @Override
    public String executeCommand(String[] args) throws Exception {

        if(args.length == 0) {
            Map<String, String> manualDescriptions = new LinkedHashMap<>();
            manualDescriptions.put("help <command>", "Help pages");
            manualDescriptions.put("exit", "Close this CLI");
            manualDescriptions.put("user", "Commands for user management");
            manualDescriptions.put("rest", "Commands for restaurants management");
            manualDescriptions.put("run <amount> <options> <command>", "Runs other commands multiple times");
            manualDescriptions.put("time <command | option>", "Use to track runtime of the specified command or receive stat data");
            manualDescriptions.put("await <ids>", "Awaits notifications on table id. Only usable in scripts. Example for ids: 1-3,4,6-9,10");
            manualDescriptions.put("listscripts", "Gives a list of the available scripts to run");
            manualDescriptions.put("script <name>", "Runs given script");
            manualDescriptions.put("log <option> <type>", "Receive or manage server saved log data");
            return "## List of commands ##\n\n" + formatManuals(manualDescriptions);
        }

        switch(args[0]) {
            case "user" -> {
                Map<String, String> manualDescriptions = new LinkedHashMap<>();
                manualDescriptions.put("list", "Lists all users");
                manualDescriptions.put("get <user_id>", "Get one user");
                manualDescriptions.put("create <name>;<email>", "Creates a new user. Name and Email may contain spaces. Email is optional");
                manualDescriptions.put("edit <user_id> <key> <value>", "Edits one value of user. Values may contain spaces");
                manualDescriptions.put("delete <user_id>", "Deletes a user");
                manualDescriptions.put("drop", "Deletes all users");
                manualDescriptions.put("flag <user_id> <restaurant_id>", "Flag a user");
                manualDescriptions.put("unflag <user_id> <restaurant_id>", "Unflag a user");

                return "## List of subcommands for 'user' ##\n\n" + formatManuals(manualDescriptions);
            }
            case "rest" -> {
                Map<String, String> manualDescriptions = new LinkedHashMap<>();
                manualDescriptions.put("list", "Lists all restaurants");
                manualDescriptions.put("get <restaurant_id>", "Get one restaurant");
                manualDescriptions.put("create <user_id>", "Creates a new restaurant. User is the owner");
                manualDescriptions.put("edit <restaurant_id> <key> <value>", "Edits one value of restaurant. Values may contain spaces");
                manualDescriptions.put("delete <restaurant_id>", "Deletes a user");
                manualDescriptions.put("drop", "Deletes all restaurants");
                manualDescriptions.put("tag <restaurant_id> <tag>", "Set tag for a restaurant");
                manualDescriptions.put("untag <restaurant_id> <tag>", "Remove a tag from a restaurant");
                manualDescriptions.put("sub <restaurant_id>", "Subscribes to updates of tables of a restaurant");
                manualDescriptions.put("unsub <restaurant_id>", "Unsubscribes from updates of tables of a restaurant");
                manualDescriptions.put("get-subs", "Get list of subscribed restaurants");
                return "## List of subcommands for 'rest' ##\n\n" + formatManuals(manualDescriptions);
            }
            case "run" -> {
                return """
                        ## Run Command ##
                        Usage: run <amount> <options> <command>
                        Explanation: Runs a command multiple times.
                        Options are --mt for multithreading and --rate <executions per second> to rate limit to a specific number.
                        The command is formatted like the single-ran commands.
                        You can use the iteration count in arguments with placeholder expressions, which can be mathematically formatted
                        Example: 'run 100 0 user create [x];[(x+1)%2]@mail.com' creates a user with the names from 0 to 99 and the email with the reversed parity of the name.
                        Using placeholder expressions might increase runtime of the command.""";
            }
            case "time" -> {
                return """
                        ## Time Command ##
                        Usage: time <command | option>
                        Explanation: Use to track runtime of the specified command or receive stat data.
                        Simple use: time <command> to give out and store time the command needed.
                        Options are:
                            --avg: Return the avg of stored times
                            --med: Return the median of stored times
                            --max: Return the maximum of stored times
                            --flush: Delete all stored times
                        """;
            }
            case "log" -> {
                return """
                        ## Log Command ##
                        Usage: log <options> <type>
                        Explanation: Receive or manage server saved log data
                        Use option --count to receive amount of logs saved of given type.
                        Type can be:
                            all - All log entries
                            client-error - Log entries with client error
                            server-error - Log entries with server error
                            success - All log entries
                            avg-time - Average server internal response time
                            med-time - Median server internal response time
                            max-time - Maximum server internal response time
                            flush - Delete server logs""";
            }
            default -> {
                throw new IllegalArgumentException("No help page available for '" + args[0] + "'");
            }
        }
    }

    private static String formatManuals(Map<String, String> commands) {
        if (commands == null || commands.isEmpty()) {
            return "";
        }

        int maxKeyLen = 0;
        for (String key : commands.keySet()) {
            if (key != null) {
                maxKeyLen = Math.max(maxKeyLen, key.length());
            }
        }

        String separator = " - ";
        int prefixWidth = maxKeyLen + separator.length();

        StringBuilder sb = new StringBuilder();
        String lineSep = System.lineSeparator();

        for (Map.Entry<String, String> entry : commands.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();

            int padding = maxKeyLen - key.length();
            String pad = padding > 0 ? " ".repeat(padding) : "";

            String[] valueLines = value.split("\\r?\\n", -1);

            sb.append(key)
                    .append(pad)
                    .append(separator)
                    .append(valueLines.length > 0 ? valueLines[0] : "")
                    .append(lineSep);

            String indent = " ".repeat(prefixWidth);
            for (int i = 1; i < valueLines.length; i++) {
                sb.append(indent).append(valueLines[i]).append(lineSep);
            }
        }

        return sb.toString();
    }
}
