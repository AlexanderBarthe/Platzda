package eva.platzda.cli.commands.scripts;

import eva.platzda.cli.ExpressionEvaluator;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.notification_management.NotificationReceiver;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
        List<String> commands = scriptLoader.getScript(args[0]);

        ConcurrentHashMap<Long, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

        for(String command : commands){
            if(command.startsWith("await")) {
                if(command.split(" ").length < 2){
                    continue;
                }

                List<Long> awaitedIds = resolveAwaitedIds(command.split(" ")[1]);

                for(long awaitedId : awaitedIds){

                    CompletableFuture<String> future = new CompletableFuture<>();

                    NotificationReceiver receiver = new NotificationReceiver(awaitedId, "notification", line -> {
                        if (line == null) return;

                        if ("__CONNECTION_CLOSED__".equals(line)) {
                            future.completeExceptionally(new IllegalStateException("Connection closed"));
                            return;
                        }

                        future.complete(line);
                    });
                    subscriptionService.addNotificationReciever(receiver);

                    futures.put(awaitedId, future);

                }

            }
            else {
                consoleManager.runCommand(command);
            }
        }

        for(CompletableFuture<String> future : futures.values()){
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("An error occurred while executing the script command.");
            }
        }

        return "Script executed.";
    }

    private List<Long> resolveAwaitedIds(String input) {

        String[] split = input.split(",");

        List<Long> ids = new ArrayList<>();
        for(String s : split){
            ids.addAll(resolveAwaitedIdRanges(s));
        }

        return ids;

    }

    private List<Long> resolveAwaitedIdRanges(String input) {

        if(!input.contains("-")){
            try {
                long id = Integer.parseInt(input);
                return List.of(id);
            } catch (NumberFormatException e) {
                return List.of();
            }
        }

        String[] split = input.split("-");

        List<Long> ids = new ArrayList<>();

        try {
            long lower = Integer.parseInt(split[0]);
            long upper = Integer.parseInt(split[1]);
            for(long i = lower; i <= upper; i++){
                ids.add(i);
            }
        } catch (NumberFormatException e) {
            return List.of();
        }

        return ids;

    }
}
