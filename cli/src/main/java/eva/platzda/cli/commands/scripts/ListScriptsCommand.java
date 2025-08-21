package eva.platzda.cli.commands.scripts;

import eva.platzda.cli.commands.execution.ConsoleCommand;

public class ListScriptsCommand implements ConsoleCommand {

    private ScriptLoader scriptLoader;

    public ListScriptsCommand(ScriptLoader scriptLoader) {
        this.scriptLoader = scriptLoader;
    }

    @Override
    public String command() {
        return "listscripts";
    }

    @Override
    public String executeCommand(String[] args) {

        StringBuilder listScripts = new StringBuilder();

        listScripts.append("Available scripts:\n");

        for (String scriptName : scriptLoader.listScripts()) {
            listScripts.append(scriptName).append("\n");
        }

        return listScripts.toString();
    }
}
