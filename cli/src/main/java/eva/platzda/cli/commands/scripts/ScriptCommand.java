package eva.platzda.cli.commands.scripts;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.List;

public class ScriptCommand implements ConsoleCommand {

    private SubscriptionService subscriptionService;
    private ScriptLoader scriptLoader;

    public ScriptCommand(SubscriptionService subscriptionService, ScriptLoader scriptLoader) {
        this.subscriptionService = subscriptionService;
        this.scriptLoader = scriptLoader;
    }

    @Override
    public String command() {
        return "script";
    }

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            return "Not enough arguments provided. See 'help' for more information.";
        }

        if(!scriptLoader.hasScript(args[0])){
            return "Script not found: " + args[0];
        }

        ConsoleManager consoleManager = new ConsoleManager(subscriptionService, scriptLoader);
        List<String> scripts = scriptLoader.getScript(args[0]);

        for(String script : scripts){
            consoleManager.runCommand(script);
        }

        return "Script executed.";
    }
}
