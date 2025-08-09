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
    public String executeCommand(String[] args) {

        if(args.length == 0) {
            Map<String, String> manualDescriptions = new LinkedHashMap<>();
            manualDescriptions.put("help", "This page");
            manualDescriptions.put("exit", "Close this CLI");
            manualDescriptions.put("user", "Commands for user management");
            manualDescriptions.put("rest", "Commands for restaurants management");
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
                return "## List of subcommands for 'rest' ##\n\n" + formatManuals(manualDescriptions);
            }
            default -> {
                return "No help page available for '" + args[0] + "'";
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
